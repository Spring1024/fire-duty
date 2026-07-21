package task

import (
	"errors"
	"sort"
	"sync"
	"time"

	"github.com/rs/zerolog/log"
)

// Task represents an inspection task.
type Task struct {
	ID           int64  `json:"id"`
	DeviceCode   string `json:"deviceCode"`
	DeviceName   string `json:"deviceName"`
	DeviceType   string `json:"deviceType"`
	TemplateID   int64  `json:"templateId"`
	TemplateName string `json:"templateName"`
	Location     string `json:"location"`
	Assignee     string `json:"assignee"`
	AssigneeID   int64  `json:"assigneeId"`
	Deadline     string `json:"deadline"`
	Status       string `json:"status"` // 待检查|已完成|已超时
	Remark       string `json:"remark"`
	CreatedAt    string `json:"createdAt"`
	CompletedAt  string `json:"completedAt,omitempty"`
}

// ListQuery holds pagination and filter parameters for listing tasks.
type ListQuery struct {
	Page     int    `form:"page" json:"page"`
	PageSize int    `form:"pageSize" json:"pageSize"`
	Tab      string `form:"tab" json:"tab"` // pending|completed|overdue
}

// TaskListResult is the paginated result of a task list query with counts.
type TaskListResult struct {
	Items    []*Task `json:"items"`
	Total    int64   `json:"total"`
	Page     int     `json:"page"`
	PageSize int     `json:"pageSize"`
	Counts   Counts  `json:"counts"`
}

// Counts holds task counts by status.
type Counts struct {
	Pending   int `json:"pending"`
	Completed int `json:"completed"`
	Overdue   int `json:"overdue"`
}

// SubmitRequest is the request body for submitting inspection results.
type SubmitRequest struct {
	Remark string `json:"remark"`
}

// Service handles all task business logic with an in-memory store.
type Service struct {
	mu            sync.RWMutex
	tasks         map[int64]*Task
	nextID        int64
	templates     map[int64]*Template
	nextTemplateID int64
}

// NewService creates a new task Service with seed data.
func NewService() *Service {
	s := &Service{
		tasks:         make(map[int64]*Task),
		nextID:        1,
		templates:     make(map[int64]*Template),
		nextTemplateID: 1,
	}
	s.seedTasks()
	s.seedTemplates()
	return s
}

// seedTasks populates the in-memory store with initial task data.
// Data matches the frontend TaskManagement.vue mock data (4 pending + 4 completed + 3 overdue).
func (s *Service) seedTasks() {
	now := time.Now()
	format := "2006-01-02 15:04:05"
	dateFormat := "2006-01-02"

	// Helper: create a date string relative to now
	pastDay := func(days int) string {
		return now.AddDate(0, 0, -days).Format(dateFormat)
	}

	seed := []Task{
		// 4 Pending tasks (待检查)
		{
			DeviceCode:   "EXT-001",
			DeviceName:   "3层走廊灭火器",
			DeviceType:   "灭火器",
			TemplateID:   1,
			TemplateName: "月度灭火器检查表",
			Location:     "A栋-3层走廊东侧",
			Assignee:     "张工",
			AssigneeID:   2,
			Deadline:     pastDay(2),
			Status:       "待检查",
			CreatedAt:    now.AddDate(0, 0, -7).Format(format),
		},
		{
			DeviceCode:   "HYD-008",
			DeviceName:   "东侧消火栓",
			DeviceType:   "消火栓",
			TemplateID:   2,
			TemplateName: "月度消火栓检查表",
			Location:     "A栋-1层东侧",
			Assignee:     "李工",
			AssigneeID:   3,
			Deadline:     pastDay(1),
			Status:       "待检查",
			CreatedAt:    now.AddDate(0, 0, -5).Format(format),
		},
		{
			DeviceCode:   "SEN-031",
			DeviceName:   "C栋温感",
			DeviceType:   "温感探测器",
			TemplateID:   3,
			TemplateName: "季度烟感检测表",
			Location:     "C栋-5层501室",
			Assignee:     "张工",
			AssigneeID:   2,
			Deadline:     pastDay(5),
			Status:       "待检查",
			CreatedAt:    now.AddDate(0, 0, -10).Format(format),
		},
		{
			DeviceCode:   "SPR-015",
			DeviceName:   "地下车库喷淋",
			DeviceType:   "喷淋系统",
			TemplateID:   4,
			TemplateName: "季度喷淋检测表",
			Location:     "B1层车库B区",
			Assignee:     "王工",
			AssigneeID:   4,
			Deadline:     pastDay(3),
			Status:       "待检查",
			CreatedAt:    now.AddDate(0, 0, -8).Format(format),
		},
		// 4 Completed tasks (已完成)
		{
			DeviceCode:   "EXT-003",
			DeviceName:   "大厅灭火器",
			DeviceType:   "灭火器",
			TemplateID:   1,
			TemplateName: "月度灭火器检查表",
			Location:     "A栋-1层大厅",
			Assignee:     "张工",
			AssigneeID:   2,
			Deadline:     pastDay(10),
			Status:       "已完成",
			Remark:       "所有检查项均正常",
			CreatedAt:    now.AddDate(0, 0, -14).Format(format),
			CompletedAt:  now.AddDate(0, 0, -10).Format(format),
		},
		{
			DeviceCode:   "HYD-012",
			DeviceName:   "西侧消火栓",
			DeviceType:   "消火栓",
			TemplateID:   2,
			TemplateName: "月度消火栓检查表",
			Location:     "C栋-2层西侧",
			Assignee:     "李工",
			AssigneeID:   3,
			Deadline:     pastDay(12),
			Status:       "已完成",
			Remark:       "水压正常，设备完好",
			CreatedAt:    now.AddDate(0, 0, -16).Format(format),
			CompletedAt:  now.AddDate(0, 0, -12).Format(format),
		},
		{
			DeviceCode:   "SEN-023",
			DeviceName:   "B栋烟感",
			DeviceType:   "烟感探测器",
			TemplateID:   3,
			TemplateName: "季度烟感检测表",
			Location:     "B栋-3层305室",
			Assignee:     "王工",
			AssigneeID:   4,
			Deadline:     pastDay(20),
			Status:       "已完成",
			Remark:       "测试正常，已清洁",
			CreatedAt:    now.AddDate(0, 0, -25).Format(format),
			CompletedAt:  now.AddDate(0, 0, -20).Format(format),
		},
		{
			DeviceCode:   "SPR-022",
			DeviceName:   "A栋车库喷淋",
			DeviceType:   "喷淋系统",
			TemplateID:   4,
			TemplateName: "季度喷淋检测表",
			Location:     "A栋-B1层车库A区",
			Assignee:     "张工",
			AssigneeID:   2,
			Deadline:     pastDay(18),
			Status:       "已完成",
			Remark:       "末端试水正常，信号阀开启",
			CreatedAt:    now.AddDate(0, 0, -22).Format(format),
			CompletedAt:  now.AddDate(0, 0, -18).Format(format),
		},
		// 3 Overdue tasks (已超时)
		{
			DeviceCode:   "EXT-005",
			DeviceName:   "食堂灭火器",
			DeviceType:   "灭火器",
			TemplateID:   1,
			TemplateName: "月度灭火器检查表",
			Location:     "A栋-2层食堂",
			Assignee:     "李工",
			AssigneeID:   3,
			Deadline:     pastDay(15),
			Status:       "已超时",
			CreatedAt:    now.AddDate(0, 0, -20).Format(format),
		},
		{
			DeviceCode:   "SEN-045",
			DeviceName:   "A栋烟感",
			DeviceType:   "烟感探测器",
			TemplateID:   3,
			TemplateName: "季度烟感检测表",
			Location:     "A栋-4层401室",
			Assignee:     "王工",
			AssigneeID:   4,
			Deadline:     pastDay(30),
			Status:       "已超时",
			CreatedAt:    now.AddDate(0, 0, -35).Format(format),
		},
		{
			DeviceCode:   "HYD-020",
			DeviceName:   "南侧消火栓",
			DeviceType:   "消火栓",
			TemplateID:   2,
			TemplateName: "月度消火栓检查表",
			Location:     "C栋-1层南侧",
			Assignee:     "张工",
			AssigneeID:   2,
			Deadline:     pastDay(25),
			Status:       "已超时",
			CreatedAt:    now.AddDate(0, 0, -30).Format(format),
		},
	}

	for i := range seed {
		seed[i].ID = s.nextID
		s.tasks[s.nextID] = &seed[i]
		s.nextID++
	}

	log.Info().Int("count", len(seed)).Msg("seeded in-memory tasks")
}

// List returns a paginated, tab-filtered list of tasks with counts.
func (s *Service) List(q ListQuery) (*TaskListResult, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if q.Page < 1 {
		q.Page = 1
	}
	if q.PageSize < 1 {
		q.PageSize = 20
	}

	// Compute counts
	var counts Counts
	for _, t := range s.tasks {
		switch t.Status {
		case "待检查":
			counts.Pending++
		case "已完成":
			counts.Completed++
		case "已超时":
			counts.Overdue++
		}
	}

	// Filter by tab
	var filtered []*Task
	for _, t := range s.tasks {
		if q.Tab != "" && t.Status != statusFromTab(q.Tab) {
			continue
		}
		filtered = append(filtered, t)
	}

	// Sort by ID descending (newest first)
	sort.Slice(filtered, func(i, j int) bool {
		return filtered[i].ID > filtered[j].ID
	})

	total := int64(len(filtered))
	start := (q.Page - 1) * q.PageSize
	if start >= int(total) {
		start = 0
	}
	end := start + q.PageSize
	if end > int(total) {
		end = int(total)
	}

	var items []*Task
	if start < len(filtered) {
		items = filtered[start:end]
	} else {
		items = []*Task{}
	}

	return &TaskListResult{
		Items:    items,
		Total:    total,
		Page:     q.Page,
		PageSize: q.PageSize,
		Counts:   counts,
	}, nil
}

// statusFromTab converts tab parameter to status string.
func statusFromTab(tab string) string {
	switch tab {
	case "pending":
		return "待检查"
	case "completed":
		return "已完成"
	case "overdue":
		return "已超时"
	default:
		return tab
	}
}

// Get retrieves a single task by ID.
func (s *Service) Get(id int64) (*Task, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	t, ok := s.tasks[id]
	if !ok {
		return nil, errors.New("任务不存在")
	}
	return t, nil
}

// Create adds a new task to the store.
func (s *Service) Create(t *Task) (*Task, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now().Format("2006-01-02 15:04:05")
	t.ID = s.nextID
	t.Status = "待检查"
	t.CreatedAt = now

	s.tasks[t.ID] = t
	s.nextID++

	log.Info().Int64("id", t.ID).Str("deviceCode", t.DeviceCode).Msg("created task")
	return t, nil
}

// Submit completes a task with inspection results.
func (s *Service) Submit(id int64, req *SubmitRequest) (*Task, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	t, ok := s.tasks[id]
	if !ok {
		return nil, errors.New("任务不存在")
	}

	if t.Status == "已完成" {
		return nil, errors.New("任务已完成，不可重复提交")
	}

	now := time.Now().Format("2006-01-02 15:04:05")
	t.Status = "已完成"
	t.CompletedAt = now
	if req.Remark != "" {
		t.Remark = req.Remark
	}

	log.Info().Int64("id", id).Msg("submitted task")
	return t, nil
}

// ListTemplates returns all templates.
func (s *Service) ListTemplates() []*Template {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]*Template, 0, len(s.templates))
	for _, tmpl := range s.templates {
		result = append(result, tmpl)
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].ID < result[j].ID
	})

	return result
}

// CreateTemplate adds a new template to the store.
func (s *Service) CreateTemplate(t *Template) (*Template, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	t.ID = s.nextTemplateID
	t.ItemCount = len(t.Items)
	for j := range t.Items {
		t.Items[j].ID = int64(j + 1)
	}

	s.templates[t.ID] = t
	s.nextTemplateID++

	log.Info().Int64("id", t.ID).Str("name", t.Name).Msg("created template")
	return t, nil
}

// GetTemplate retrieves a single template by ID.
func (s *Service) GetTemplate(id int64) (*Template, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	t, ok := s.templates[id]
	if !ok {
		return nil, errors.New("模板不存在")
	}
	return t, nil
}
