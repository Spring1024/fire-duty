package user

import (
	"errors"
	"fmt"
	"math"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/rs/zerolog/log"
	"golang.org/x/crypto/bcrypt"
)

// User represents a system user.
type User struct {
	ID           int64   `json:"id"`
	Name         string  `json:"name"`
	Username     string  `json:"username"`
	PasswordHash string  `json:"-"` // bcrypt hash, never serialized
	Role         string  `json:"role"`
	GridID       int64   `json:"gridId,omitempty"`
	GridName     string  `json:"gridName,omitempty"`
	Phone        string  `json:"phone,omitempty"`
	Status       string  `json:"status"` // 正常|停用
	LastLogin    string  `json:"lastLogin,omitempty"`
	CreatedAt    string  `json:"createdAt"`
	UpdatedAt    string  `json:"updatedAt"`
}

// ListQuery holds pagination and filter parameters for listing users.
type ListQuery struct {
	Page     int    `form:"page"`
	PageSize int    `form:"pageSize"`
	Role     string `form:"role"`
	Status   string `form:"status"`
	Search   string `form:"search"`
}

// ListResult is the paginated result of a user list query.
type ListResult struct {
	Items    []*User `json:"items"`
	Total    int64   `json:"total"`
	Page     int     `json:"page"`
	PageSize int     `json:"pageSize"`
}

// Service handles all user business logic with an in-memory store.
type Service struct {
	mu     sync.RWMutex
	users  map[int64]*User
	nextID int64
}

// NewService creates a new user Service with seed data.
func NewService() *Service {
	s := &Service{
		users:  make(map[int64]*User),
		nextID: 1,
	}
	s.seed()
	return s
}

// seed populates the in-memory store with initial user data.
// Data matches the frontend UserManagement.vue mock data (5 users).
func (s *Service) seed() {
	now := time.Now().Format(time.RFC3339)
	// Default password "123456" hashed with bcrypt
	defaultHash := hashPassword("123456")
	adminHash := hashPassword("admin123")

	seed := []User{
		{
			Name:         "系统管理员",
			Username:     "admin",
			PasswordHash: adminHash,
			Role:         "超级管理员",
			Phone:        "13800000001",
			Status:       "正常",
			LastLogin:    "2025-07-21 08:30:00",
		},
		{
			Name:         "张三",
			Username:     "zhangsan",
			PasswordHash: defaultHash,
			Role:         "大网格负责人",
			GridID:       1,
			GridName:     "科技产业园",
			Phone:        "13800000002",
			Status:       "正常",
			LastLogin:    "2025-07-20 09:15:00",
		},
		{
			Name:         "李四",
			Username:     "lisi",
			PasswordHash: defaultHash,
			Role:         "中网格组长",
			GridID:       2,
			GridName:     "A栋",
			Phone:        "13800000003",
			Status:       "正常",
			LastLogin:    "2025-07-19 14:20:00",
		},
		{
			Name:         "王五",
			Username:     "wangwu",
			PasswordHash: defaultHash,
			Role:         "小网格检查员",
			GridID:       5,
			GridName:     "A栋-3层",
			Phone:        "13800000004",
			Status:       "正常",
			LastLogin:    "2025-07-18 10:00:00",
		},
		{
			Name:         "赵六",
			Username:     "zhaoliu",
			PasswordHash: defaultHash,
			Role:         "小网格检查员",
			GridID:       3,
			GridName:     "B栋",
			Phone:        "13800000005",
			Status:       "停用",
			LastLogin:    "2025-06-30 16:45:00",
		},
	}

	for i := range seed {
		seed[i].ID = s.nextID
		seed[i].CreatedAt = now
		seed[i].UpdatedAt = now
		s.users[s.nextID] = &seed[i]
		s.nextID++
	}

	log.Info().Int("count", len(seed)).Msg("seeded in-memory users")
}

// hashPassword hashes a plaintext password using bcrypt.
func hashPassword(password string) string {
	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		log.Warn().Err(err).Msg("failed to hash password, using raw")
		return password
	}
	return string(hash)
}

// List returns a paginated, filtered list of users.
func (s *Service) List(q ListQuery) (*ListResult, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if q.Page < 1 {
		q.Page = 1
	}
	if q.PageSize < 1 {
		q.PageSize = 20
	}

	var filtered []*User
	for _, u := range s.users {
		// Search filter (name or username)
		if q.Search != "" {
			search := strings.ToLower(q.Search)
			if !strings.Contains(strings.ToLower(u.Name), search) &&
				!strings.Contains(strings.ToLower(u.Username), search) {
				continue
			}
		}
		// Role filter
		if q.Role != "" && u.Role != q.Role {
			continue
		}
		// Status filter
		if q.Status != "" && u.Status != q.Status {
			continue
		}
		filtered = append(filtered, u)
	}

	// Sort by ID descending (newest first)
	sort.Slice(filtered, func(i, j int) bool {
		return filtered[i].ID > filtered[j].ID
	})

	total := int64(len(filtered))
	totalPages := int(math.Ceil(float64(total) / float64(q.PageSize)))
	if q.Page > totalPages && totalPages > 0 {
		q.Page = totalPages
	}

	start := (q.Page - 1) * q.PageSize
	if start >= int(total) {
		start = 0
	}

	end := start + q.PageSize
	if end > int(total) {
		end = int(total)
	}

	var items []*User
	if start < len(filtered) {
		items = filtered[start:end]
	} else {
		items = []*User{}
	}

	return &ListResult{
		Items:    items,
		Total:    total,
		Page:     q.Page,
		PageSize: q.PageSize,
	}, nil
}

// Get retrieves a single user by ID.
func (s *Service) Get(id int64) (*User, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	u, ok := s.users[id]
	if !ok {
		return nil, errors.New("用户不存在")
	}
	return u, nil
}

// Create adds a new user to the store. Automatically hashes the password.
func (s *Service) Create(u *User) (*User, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	// Check username uniqueness
	for _, existing := range s.users {
		if existing.Username == u.Username {
			return nil, fmt.Errorf("用户名 '%s' 已存在", u.Username)
		}
	}

	now := time.Now().Format(time.RFC3339)
	u.ID = s.nextID
	u.PasswordHash = hashPassword(u.PasswordHash) // hash the raw password
	u.Status = "正常"
	u.CreatedAt = now
	u.UpdatedAt = now

	s.users[u.ID] = u
	s.nextID++

	log.Info().Int64("id", u.ID).Str("username", u.Username).Msg("created user")
	return u, nil
}

// Update modifies an existing user.
func (s *Service) Update(id int64, updated *User) (*User, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	existing, ok := s.users[id]
	if !ok {
		return nil, errors.New("用户不存在")
	}

	// Check username uniqueness if changed
	if updated.Username != "" && updated.Username != existing.Username {
		for _, other := range s.users {
			if other.ID != id && other.Username == updated.Username {
				return nil, fmt.Errorf("用户名 '%s' 已被使用", updated.Username)
			}
		}
	}

	now := time.Now().Format(time.RFC3339)

	if updated.Name != "" {
		existing.Name = updated.Name
	}
	if updated.Username != "" {
		existing.Username = updated.Username
	}
	if updated.PasswordHash != "" {
		existing.PasswordHash = hashPassword(updated.PasswordHash)
	}
	if updated.Role != "" {
		existing.Role = updated.Role
	}
	if updated.GridID >= 0 {
		existing.GridID = updated.GridID
	}
	if updated.GridName != "" {
		existing.GridName = updated.GridName
	}
	if updated.Phone != "" {
		existing.Phone = updated.Phone
	}
	if updated.Status != "" {
		existing.Status = updated.Status
	}
	existing.UpdatedAt = now

	log.Info().Int64("id", id).Msg("updated user")
	return existing, nil
}

// Delete removes a user from the store.
func (s *Service) Delete(id int64) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	if _, ok := s.users[id]; !ok {
		return errors.New("用户不存在")
	}

	delete(s.users, id)
	log.Info().Int64("id", id).Msg("deleted user")
	return nil
}
