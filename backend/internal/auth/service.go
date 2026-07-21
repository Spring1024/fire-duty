package auth

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/rs/zerolog/log"
	"golang.org/x/crypto/bcrypt"

	"github.com/spring1024/fire-duty/internal/middleware"
)

// User represents an authenticated user in the system.
type User struct {
	ID          int      `json:"id"`
	Name        string   `json:"name"`
	Username    string   `json:"username"`
	Password    string   `json:"-"` // bcrypt hash, never serialized
	Role        string   `json:"role"`
	Permissions []string `json:"permissions"`
}

// LoginResponse is the payload returned on successful login.
type LoginResponse struct {
	Token        string `json:"token"`
	RefreshToken string `json:"refreshToken"`
	User         User   `json:"user"`
}

// TokenResponse is the payload returned on token refresh.
type TokenResponse struct {
	Token        string `json:"token"`
	RefreshToken string `json:"refreshToken"`
}

// CustomClaims extends jwt.RegisteredClaims with application-specific fields.
type CustomClaims struct {
	UserID      int      `json:"userID"`
	Username    string   `json:"username"`
	Role        string   `json:"role"`
	Permissions []string `json:"permissions"`
	jwt.RegisteredClaims
}

// Service handles all authentication business logic.
type Service struct {
	db              *pgxpool.Pool
	jwtSecret       []byte
	accessTokenTTL  time.Duration
	refreshTokenTTL time.Duration
	users           []User // in-memory user store (will be replaced by DB queries later)
}

// NewService creates a new auth Service with an in-memory user store.
// If db is nil, the service operates purely in-memory.
func NewService(db *pgxpool.Pool, jwtSecret string) *Service {
	s := &Service{
		db:              db,
		jwtSecret:       []byte(jwtSecret),
		accessTokenTTL:  2 * time.Hour,
		refreshTokenTTL: 7 * 24 * time.Hour,
	}
	s.seedUsers()
	return s
}

// seedUsers populates the in-memory user store with test users.
// Passwords are bcrypt-hashed at startup.
func (s *Service) seedUsers() {
	rawUsers := []struct {
		id          int
		name        string
		username    string
		password    string
		role        string
	}{
		{1, "管理员", "admin", "admin123", "超级管理员"},
		{2, "张三", "zhangsan", "123456", "大网格负责人"},
		{3, "李四", "lisi", "123456", "中网格组长"},
		{4, "王五", "wangwu", "123456", "小网格检查员"},
	}

	for _, ru := range rawUsers {
		hash, err := bcrypt.GenerateFromPassword([]byte(ru.password), bcrypt.DefaultCost)
		if err != nil {
			log.Warn().Err(err).Str("username", ru.username).Msg("failed to hash password for seed user")
			continue
		}

		perms := middleware.RolePermissions[ru.role]
		if perms == nil {
			perms = []string{}
		}

		s.users = append(s.users, User{
			ID:          ru.id,
			Name:        ru.name,
			Username:    ru.username,
			Password:    string(hash),
			Role:        ru.role,
			Permissions: perms,
		})
	}
	log.Info().Int("count", len(s.users)).Msg("seeded in-memory users")
}

// findUserByUsername looks up a user by username in the in-memory store.
func (s *Service) findUserByUsername(username string) *User {
	for i := range s.users {
		if s.users[i].Username == username {
			return &s.users[i]
		}
	}
	return nil
}

// findUserByID looks up a user by ID in the in-memory store.
func (s *Service) findUserByID(id int) *User {
	for i := range s.users {
		if s.users[i].ID == id {
			return &s.users[i]
		}
	}
	return nil
}

// Login authenticates a user by username and password.
// Returns JWT access token, refresh token, user info, or an error.
func (s *Service) Login(ctx context.Context, username, password string) (string, string, User, error) {
	user := s.findUserByUsername(username)
	if user == nil {
		return "", "", User{}, errors.New("用户名或密码错误")
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(password)); err != nil {
		return "", "", User{}, errors.New("用户名或密码错误")
	}

	// Generate tokens
	accessToken, err := s.generateToken(user, s.accessTokenTTL)
	if err != nil {
		return "", "", User{}, fmt.Errorf("生成 token 失败: %w", err)
	}

	refreshToken, err := s.generateToken(user, s.refreshTokenTTL)
	if err != nil {
		return "", "", User{}, fmt.Errorf("生成 refresh token 失败: %w", err)
	}

	return accessToken, refreshToken, *user, nil
}

// RefreshToken validates a refresh token and issues a new token pair.
func (s *Service) RefreshToken(ctx context.Context, refreshToken string) (string, string, error) {
	claims := &CustomClaims{}
	token, err := jwt.ParseWithClaims(refreshToken, claims, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return s.jwtSecret, nil
	})
	if err != nil || !token.Valid {
		return "", "", errors.New("refresh token 无效或已过期")
	}

	// Find user to ensure they still exist
	user := s.findUserByID(claims.UserID)
	if user == nil {
		return "", "", errors.New("用户不存在")
	}

	// Generate new tokens
	newAccess, err := s.generateToken(user, s.accessTokenTTL)
	if err != nil {
		return "", "", fmt.Errorf("生成 token 失败: %w", err)
	}

	newRefresh, err := s.generateToken(user, s.refreshTokenTTL)
	if err != nil {
		return "", "", fmt.Errorf("生成 refresh token 失败: %w", err)
	}

	return newAccess, newRefresh, nil
}

// GetUserByID retrieves a user by their ID.
func (s *Service) GetUserByID(ctx context.Context, id int) (User, error) {
	user := s.findUserByID(id)
	if user == nil {
		return User{}, errors.New("用户不存在")
	}
	return *user, nil
}

// ChangePassword verifies the old password and updates to the new password.
func (s *Service) ChangePassword(ctx context.Context, userID int, oldPassword, newPassword string) error {
	user := s.findUserByID(userID)
	if user == nil {
		return errors.New("用户不存在")
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(oldPassword)); err != nil {
		return errors.New("原密码错误")
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		return fmt.Errorf("密码加密失败: %w", err)
	}

	// Update in-memory
	for i := range s.users {
		if s.users[i].ID == userID {
			s.users[i].Password = string(hash)
			break
		}
	}

	return nil
}

// generateToken creates a signed JWT token for the given user with the specified TTL.
func (s *Service) generateToken(user *User, ttl time.Duration) (string, error) {
	now := time.Now()
	claims := CustomClaims{
		UserID:      user.ID,
		Username:    user.Username,
		Role:        user.Role,
		Permissions: user.Permissions,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(now.Add(ttl)),
			IssuedAt:  jwt.NewNumericDate(now),
			Issuer:    "fire-duty",
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(s.jwtSecret)
}
