package grid

import (
	"errors"
	"fmt"
	"sort"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/rs/zerolog/log"
)

// Grid represents a management grid in a tree structure.
type Grid struct {
	ID          int64  `json:"id"`
	Name        string `json:"name"`
	Level       string `json:"level"` // 大网格|中网格|小网格
	ParentID    int64  `json:"parentId"`
	Path        string `json:"path"`        // materialized path, e.g. "/1/2/5"
	Leader      string `json:"leader,omitempty"`
	DeviceCount int    `json:"deviceCount"`
	Contact     string `json:"contact,omitempty"`
	Phone       string `json:"phone,omitempty"`
	Scope       string `json:"scope,omitempty"`
	CreatedAt   string `json:"createdAt"`
	UpdatedAt   string `json:"updatedAt"`
}

// TreeNode represents a grid node with children for tree display.
type TreeNode struct {
	ID        int64       `json:"id"`
	Name      string      `json:"name"`
	Level     string      `json:"level"`
	ParentID  int64       `json:"parentId"`
	Path      string      `json:"path"`
	Leader    string      `json:"leader,omitempty"`
	Count     int         `json:"count"`
	Contact   string      `json:"contact,omitempty"`
	Phone     string      `json:"phone,omitempty"`
	Scope     string      `json:"scope,omitempty"`
	Children  []*TreeNode `json:"children,omitempty"`
}

// Service handles all grid business logic with an in-memory store.
type Service struct {
	mu     sync.RWMutex
	grids  map[int64]*Grid
	nextID int64
}

// NewService creates a new grid Service with seed data.
func NewService() *Service {
	s := &Service{
		grids:  make(map[int64]*Grid),
		nextID: 1,
	}
	s.seed()
	return s
}

// seed populates the in-memory store with initial grid data.
// Tree structure: 科技产业园 → A/B/C栋 → A栋-3层
func (s *Service) seed() {
	now := time.Now().Format(time.RFC3339)

	seed := []Grid{
		{
			Name:        "科技产业园",
			Level:       "大网格",
			ParentID:    0,
			Path:        "/1",
			Leader:      "张三",
			DeviceCount: 4,
			Contact:     "张主任",
			Phone:       "13800000010",
			Scope:       "科技产业园全部区域，包含A/B/C三栋办公楼",
		},
		{
			Name:        "A栋",
			Level:       "中网格",
			ParentID:    1,
			Path:        "/1/2",
			Leader:      "李四",
			DeviceCount: 3,
			Contact:     "李组长",
			Phone:       "13800000011",
			Scope:       "A栋办公楼地上B1-5层及地下车库",
		},
		{
			Name:        "B栋",
			Level:       "中网格",
			ParentID:    1,
			Path:        "/1/3",
			Leader:      "王五",
			DeviceCount: 1,
			Contact:     "王组长",
			Phone:       "13800000012",
			Scope:       "B栋办公楼地上1-6层",
		},
		{
			Name:        "C栋",
			Level:       "中网格",
			ParentID:    1,
			Path:        "/1/4",
			Leader:      "赵六",
			DeviceCount: 1,
			Contact:     "赵组长",
			Phone:       "13800000013",
			Scope:       "C栋办公楼地上1-5层",
		},
		{
			Name:        "A栋-3层",
			Level:       "小网格",
			ParentID:    2,
			Path:        "/1/2/5",
			Leader:      "王五",
			DeviceCount: 1,
			Contact:     "王检查员",
			Phone:       "13800000014",
			Scope:       "A栋3层东侧301-310室",
		},
	}

	for i := range seed {
		seed[i].ID = s.nextID
		seed[i].CreatedAt = now
		seed[i].UpdatedAt = now
		s.grids[s.nextID] = &seed[i]
		s.nextID++
	}

	log.Info().Int("count", len(seed)).Msg("seeded in-memory grids")
}

// computePath computes the materialized path for a new grid.
func (s *Service) computePath(id int64, parentID int64) string {
	if parentID == 0 {
		return fmt.Sprintf("/%d", id)
	}
	parent, ok := s.grids[parentID]
	if !ok {
		return fmt.Sprintf("/%d", id)
	}
	return parent.Path + fmt.Sprintf("/%d", id)
}

// determineLevel determines the grid level based on parent.
func (s *Service) determineLevel(parentID int64) string {
	if parentID == 0 {
		return "大网格"
	}
	parent, ok := s.grids[parentID]
	if !ok {
		return "中网格"
	}
	switch parent.Level {
	case "大网格":
		return "中网格"
	case "中网格":
		return "小网格"
	default:
		return "中网格"
	}
}

// List returns all grids (flat list, for CRUD display).
func (s *Service) List() ([]*Grid, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]*Grid, 0, len(s.grids))
	for _, g := range s.grids {
		result = append(result, g)
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].ID < result[j].ID
	})

	return result, nil
}

// Tree returns the grid list as a tree structure.
func (s *Service) Tree() []*TreeNode {
	s.mu.RLock()
	defer s.mu.RUnlock()

	// Build map of parent → children
	children := make(map[int64][]*Grid)
	for _, g := range s.grids {
		children[g.ParentID] = append(children[g.ParentID], g)
	}

	// Sort children by ID
	for pid := range children {
		sort.Slice(children[pid], func(i, j int) bool {
			return children[pid][i].ID < children[pid][j].ID
		})
	}

	// Build tree from root
	var build func(parentID int64) []*TreeNode
	build = func(parentID int64) []*TreeNode {
		var nodes []*TreeNode
		for _, g := range children[parentID] {
			node := &TreeNode{
				ID:       g.ID,
				Name:     g.Name,
				Level:    g.Level,
				ParentID: g.ParentID,
				Path:     g.Path,
				Leader:   g.Leader,
				Count:    g.DeviceCount,
				Contact:  g.Contact,
				Phone:    g.Phone,
				Scope:    g.Scope,
			}
			node.Children = build(g.ID)
			nodes = append(nodes, node)
		}
		return nodes
	}

	return build(0)
}

// Get retrieves a single grid by ID.
func (s *Service) Get(id int64) (*Grid, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	g, ok := s.grids[id]
	if !ok {
		return nil, errors.New("网格不存在")
	}
	return g, nil
}

// Create adds a new grid to the store.
func (s *Service) Create(g *Grid) (*Grid, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	// Validate parent exists if set
	if g.ParentID != 0 {
		if _, ok := s.grids[g.ParentID]; !ok {
			return nil, errors.New("父级网格不存在")
		}
	}

	now := time.Now().Format(time.RFC3339)
	g.ID = s.nextID
	g.Path = s.computePath(g.ID, g.ParentID)
	if g.Level == "" {
		g.Level = s.determineLevel(g.ParentID)
	}
	g.CreatedAt = now
	g.UpdatedAt = now

	s.grids[g.ID] = g
	s.nextID++

	log.Info().Int64("id", g.ID).Str("name", g.Name).Msg("created grid")
	return g, nil
}

// Update modifies an existing grid.
func (s *Service) Update(id int64, updated *Grid) (*Grid, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	existing, ok := s.grids[id]
	if !ok {
		return nil, errors.New("网格不存在")
	}

	now := time.Now().Format(time.RFC3339)

	if updated.Name != "" {
		existing.Name = updated.Name
	}
	if updated.Leader != "" {
		existing.Leader = updated.Leader
	}
	if updated.Contact != "" {
		existing.Contact = updated.Contact
	}
	if updated.Phone != "" {
		existing.Phone = updated.Phone
	}
	if updated.Scope != "" {
		existing.Scope = updated.Scope
	}
	if updated.DeviceCount >= 0 {
		existing.DeviceCount = updated.DeviceCount
	}

	// Update level if parent changed
	if updated.ParentID > 0 && updated.ParentID != existing.ParentID {
		if _, ok := s.grids[updated.ParentID]; !ok {
			return nil, errors.New("父级网格不存在")
		}
		existing.ParentID = updated.ParentID
		existing.Level = s.determineLevel(updated.ParentID)
		existing.Path = s.rebuildPath(existing)
	}
	// Allow setting parent to 0 (root level)
	if updated.ParentID == 0 && existing.ParentID != 0 {
		existing.ParentID = 0
		existing.Level = "大网格"
		existing.Path = fmt.Sprintf("/%d", id)
	}

	existing.UpdatedAt = now

	log.Info().Int64("id", id).Msg("updated grid")
	return existing, nil
}

// rebuildPath recalculates the materialized path for a grid and its descendants.
func (s *Service) rebuildPath(g *Grid) string {
	if g.ParentID == 0 {
		return fmt.Sprintf("/%d", g.ID)
	}
	parent := s.grids[g.ParentID]
	if parent == nil {
		return fmt.Sprintf("/%d", g.ID)
	}
	return parent.Path + fmt.Sprintf("/%d", g.ID)
}

// Delete removes a grid from the store, unless it has children.
func (s *Service) Delete(id int64) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	if _, ok := s.grids[id]; !ok {
		return errors.New("网格不存在")
	}

	// Check for children
	for _, g := range s.grids {
		if g.ParentID == id {
			return errors.New("该网格下存在子网格，无法删除")
		}
	}

	delete(s.grids, id)
	log.Info().Int64("id", id).Msg("deleted grid")
	return nil
}

// GetAll returns all grids (flat list, for internal use).
func (s *Service) GetAll() []*Grid {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]*Grid, 0, len(s.grids))
	for _, g := range s.grids {
		result = append(result, g)
	}
	return result
}

// parseIDs parses a comma-separated list of grid IDs.
func parseIDs(s string) ([]int64, error) {
	if s == "" {
		return nil, nil
	}
	parts := strings.Split(s, ",")
	ids := make([]int64, 0, len(parts))
	for _, p := range parts {
		id, err := strconv.ParseInt(strings.TrimSpace(p), 10, 64)
		if err != nil {
			return nil, fmt.Errorf("invalid grid ID: %s", p)
		}
		ids = append(ids, id)
	}
	return ids, nil
}
