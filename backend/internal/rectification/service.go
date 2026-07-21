package rectification

import (
	"errors"
	"sort"
	"sync"
	"time"

	"github.com/rs/zerolog/log"
)

// Rectification represents a hazard rectification work order.
type Rectification struct {
	ID          int64  `json:"id"`
	Description string `json:"description"`
	DeviceCode  string `json:"deviceCode"`
	DeviceName  string `json:"deviceName"`
	Level       string `json:"level"`       // 紧急,一般
	LevelType   string `json:"levelType"`   // danger,warning
	FoundTime   string `json:"foundTime"`
	Assignee    string `json:"assignee"`
	AssigneeID  int64  `json:"assigneeId"`
	Status      string `json:"status"`      // 待派发|整改中|待复核|已闭环|已超时
	StatusType  string `json:"statusType"`  // warning|primary|info|success|danger
	Deadline    string `json:"deadline"`
	CreatedAt   string `json:"createdAt"`
	UpdatedAt   string `json:"updatedAt"`
}

// Timeline represents a state transition or action log entry.
type Timeline struct {
	ID        int64  `json:"id"`
	RectID    int64  `json:"rectId"`
	Action    string `json:"action"`
	Operator  string `json:"operator"`
	Comment   string `json:"comment"`
	Timestamp string `json:"timestamp"`
}

// Photo represents a before/after photo attached to a rectification.
type Photo struct {
	ID      int64  `json:"id"`
	RectID  int64  `json:"rectId"`
	Type    string `json:"type"` // before,after
	URL     string `json:"url"`
	TakenAt string `json:"takenAt"`
}

// ListQuery holds pagination and filter parameters for listing rectifications.
type ListQuery struct {
	Page     int    `form:"page" json:"page"`
	PageSize int    `form:"pageSize" json:"pageSize"`
	Tab      string `form:"tab" json:"tab"` // pending|ongoing|review|closed
}

// ListResult is the paginated result of a rectification list query with counts.
type ListResult struct {
	Items    []*Rectification `json:"items"`
	Total    int64            `json:"total"`
	Page     int              `json:"page"`
	PageSize int              `json:"pageSize"`
	Counts   RectCounts       `json:"counts"`
}

// RectCounts holds rectification counts by tab.
type RectCounts struct {
	Pending int `json:"pending"`
	Ongoing int `json:"ongoing"`
	Review  int `json:"review"`
	Closed  int `json:"closed"`
}

// GetDetailResult is the detailed view of a single rectification with timeline and photos.
type GetDetailResult struct {
	*Rectification
	Timeline []Timeline `json:"timeline"`
	Photos   []Photo    `json:"photos"`
}

// statusFromTab converts tab parameter to status string.
func statusFromTab(tab string) string {
	switch tab {
	case "pending":
		return "待派发"
	case "ongoing":
		return "整改中"
	case "review":
		return "待复核"
	case "closed":
		return "已闭环"
	default:
		return ""
	}
}

// tabFromStatus converts status to tab label.
func tabFromStatus(status string) string {
	switch status {
	case "待派发":
		return "pending"
	case "整改中", "已超时":
		return "ongoing"
	case "待复核":
		return "review"
	case "已闭环":
		return "closed"
	default:
		return ""
	}
}

// statusTypeFromStatus returns the UI status type (color) based on status.
func statusTypeFromStatus(status string) string {
	switch status {
	case "待派发":
		return "warning"
	case "整改中":
		return "primary"
	case "待复核":
		return "info"
	case "已闭环":
		return "success"
	case "已超时":
		return "danger"
	default:
		return "default"
	}
}

// Service handles all rectification business logic with an in-memory store.
type Service struct {
	mu            sync.RWMutex
	rects         map[int64]*Rectification
	timelines     map[int64][]Timeline
	photos        map[int64][]Photo
	nextRectID    int64
	nextTimelineID int64
	nextPhotoID   int64
}

// NewService creates a new rectification Service with seed data.
func NewService() *Service {
	s := &Service{
		rects:          make(map[int64]*Rectification),
		timelines:      make(map[int64][]Timeline),
		photos:         make(map[int64][]Photo),
		nextRectID:     1,
		nextTimelineID: 1,
		nextPhotoID:    1,
	}
	s.seed()
	go s.timeoutLoop()
	return s
}

// timeoutLoop periodically checks for rectifications past deadline.
func (s *Service) timeoutLoop() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()
	for range ticker.C {
		s.checkTimeouts()
	}
}

// checkTimeouts marks eligible rectifications as 已超时.
func (s *Service) checkTimeouts() {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now()
	format := "2006-01-02 15:04:05"
	dateOnly := "2006-01-02"

	for _, r := range s.rects {
		if r.Status != "待派发" && r.Status != "整改中" && r.Status != "待复核" {
			continue
		}
		if r.Deadline == "" {
			continue
		}

		var deadlineTime time.Time
		var err error
		// Try full datetime format first, then date-only
		deadlineTime, err = time.Parse(format, r.Deadline)
		if err != nil {
			deadlineTime, err = time.Parse(dateOnly, r.Deadline)
			if err != nil {
				continue
			}
		}

		if now.After(deadlineTime) {
			r.Status = "已超时"
			r.StatusType = "danger"
			r.UpdatedAt = now.Format(format)

			// Add timeout timeline entry
			tl := Timeline{
				ID:        s.nextTimelineID,
				RectID:    r.ID,
				Action:    "系统自动超时",
				Operator:  "系统",
				Comment:   "整改期限已过，系统自动标记为已超时",
				Timestamp: now.Format(format),
			}
			s.timelines[r.ID] = append(s.timelines[r.ID], tl)
			s.nextTimelineID++

			log.Info().Int64("id", r.ID).Msg("rectification auto-timed out")
		}
	}
}

// seed populates the in-memory store with initial rectification data.
func (s *Service) seed() {
	now := time.Now()
	format := "2006-01-02 15:04:05"
	dateOnly := "2006-01-02"

	pastDay := func(days int) string {
		return now.AddDate(0, 0, -days).Format(dateOnly)
	}
	pastTime := func(days int) string {
		return now.AddDate(0, 0, -days).Format(format)
	}

	// Helper: add timeline entries
	addTimeline := func(rectID int64, entries []struct {
		Action   string
		Operator string
		Comment  string
		DaysAgo  int
	}) {
		for _, e := range entries {
			tl := Timeline{
				ID:        s.nextTimelineID,
				RectID:    rectID,
				Action:    e.Action,
				Operator:  e.Operator,
				Comment:   e.Comment,
				Timestamp: pastTime(e.DaysAgo),
			}
			s.timelines[rectID] = append(s.timelines[rectID], tl)
			s.nextTimelineID++
		}
	}

	seed := []struct {
		Rect        Rectification
		TimelineDef []struct {
			Action   string
			Operator string
			Comment  string
			DaysAgo  int
		}
		Photos []struct {
			Type    string
			URL     string
			DaysAgo int
		}
	}{
		// 1. Pending (待派发)
		{
			Rect: Rectification{
				Description: "A栋-3层走廊东侧灭火器压力不足",
				DeviceCode:  "EXT-001",
				DeviceName:  "3层走廊灭火器",
				Level:       "紧急",
				LevelType:   "danger",
				FoundTime:   pastTime(3),
				Assignee:    "张工",
				AssigneeID:  2,
				Status:      "待派发",
				StatusType:  "warning",
				Deadline:    pastDay(5),
				CreatedAt:   pastTime(3),
				UpdatedAt:   pastTime(3),
			},
			TimelineDef: []struct {
				Action   string
				Operator string
				Comment  string
				DaysAgo  int
			}{
				{
					Action:   "隐患登记",
					Operator: "李工",
					Comment:  "发现A栋-3层走廊东侧灭火器压力表指针在红色区域，需立即整改",
					DaysAgo:  3,
				},
			},
			Photos: []struct {
				Type    string
				URL     string
				DaysAgo int
			}{
				{
					Type:    "before",
					URL:     "https://via.placeholder.com/400x300?text=Before+EXT-001",
					DaysAgo: 3,
				},
			},
		},
		// 2. Ongoing (整改中)
		{
			Rect: Rectification{
				Description: "B栋-3层305室烟感探测器故障",
				DeviceCode:  "SEN-023",
				DeviceName:  "B栋烟感",
				Level:       "一般",
				LevelType:   "warning",
				FoundTime:   pastTime(7),
				Assignee:    "王工",
				AssigneeID:  4,
				Status:      "整改中",
				StatusType:  "primary",
				Deadline:    pastDay(2),
				CreatedAt:   pastTime(7),
				UpdatedAt:   pastTime(2),
			},
			TimelineDef: []struct {
				Action   string
				Operator string
				Comment  string
				DaysAgo  int
			}{
				{
					Action:   "隐患登记",
					Operator: "张工",
					Comment:  "B栋-3层305室烟感探测器指示灯异常，需维修",
					DaysAgo:  7,
				},
				{
					Action:   "任务派发",
					Operator: "系统",
					Comment:  "已派发给王工（维修组）",
					DaysAgo:  6,
				},
				{
					Action:   "开始整改",
					Operator: "王工",
					Comment:  "已接单，准备前往B栋3层进行检修",
					DaysAgo:  4,
				},
				{
					Action:   "整改汇报",
					Operator: "王工",
					Comment:  "已更换烟感探测器，需复核",
					DaysAgo:  2,
				},
			},
			Photos: []struct {
				Type    string
				URL     string
				DaysAgo int
			}{
				{
					Type:    "before",
					URL:     "https://via.placeholder.com/400x300?text=Before+SEN-023",
					DaysAgo: 7,
				},
				{
					Type:    "after",
					URL:     "https://via.placeholder.com/400x300?text=After+SEN-023",
					DaysAgo: 2,
				},
			},
		},
		// 3. Closed (已闭环)
		{
			Rect: Rectification{
				Description: "C栋-2层西侧消火栓阀门渗漏",
				DeviceCode:  "HYD-012",
				DeviceName:  "西侧消火栓",
				Level:       "紧急",
				LevelType:   "danger",
				FoundTime:   pastTime(15),
				Assignee:    "李工",
				AssigneeID:  3,
				Status:      "已闭环",
				StatusType:  "success",
				Deadline:    pastDay(10),
				CreatedAt:   pastTime(15),
				UpdatedAt:   pastTime(8),
			},
			TimelineDef: []struct {
				Action   string
				Operator string
				Comment  string
				DaysAgo  int
			}{
				{
					Action:   "隐患登记",
					Operator: "王工",
					Comment:  "C栋-2层西侧消火栓阀门处发现渗漏，地面有积水",
					DaysAgo:  15,
				},
				{
					Action:   "任务派发",
					Operator: "系统",
					Comment:  "已派发给李工（维修组），要求3日内完成",
					DaysAgo:  14,
				},
				{
					Action:   "开始整改",
					Operator: "李工",
					Comment:  "已接单，携带工具前往C栋2层",
					DaysAgo:  13,
				},
				{
					Action:   "整改汇报",
					Operator: "李工",
					Comment:  "已更换阀门密封圈，渗漏已处理，消火栓功能正常",
					DaysAgo:  11,
				},
				{
					Action:   "复核通过",
					Operator: "张工",
					Comment:  "现场复核确认渗漏已修复，消火栓功能正常，通过验收",
					DaysAgo:  10,
				},
				{
					Action:   "闭环确认",
					Operator: "系统",
					Comment:  "隐患整改完成，已闭环归档",
					DaysAgo:  10,
				},
			},
			Photos: []struct {
				Type    string
				URL     string
				DaysAgo int
			}{
				{
					Type:    "before",
					URL:     "https://via.placeholder.com/400x300?text=Before+HYD-012",
					DaysAgo: 15,
				},
				{
					Type:    "after",
					URL:     "https://via.placeholder.com/400x300?text=After+HYD-012",
					DaysAgo: 11,
				},
			},
		},
	}

	for _, sdata := range seed {
		rect := sdata.Rect
		rect.ID = s.nextRectID
		s.rects[s.nextRectID] = &rect
		s.nextRectID++

		// Add timeline
		addTimeline(rect.ID, sdata.TimelineDef)

		// Add photos
		for _, p := range sdata.Photos {
			photo := Photo{
				ID:      s.nextPhotoID,
				RectID:  rect.ID,
				Type:    p.Type,
				URL:     p.URL,
				TakenAt: pastTime(p.DaysAgo),
			}
			s.photos[rect.ID] = append(s.photos[rect.ID], photo)
			s.nextPhotoID++
		}
	}

	log.Info().Int("count", len(seed)).Msg("seeded in-memory rectifications")
}

// List returns a paginated, tab-filtered list of rectifications with counts.
func (s *Service) List(q ListQuery) (*ListResult, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if q.Page < 1 {
		q.Page = 1
	}
	if q.PageSize < 1 {
		q.PageSize = 20
	}

	// Compute counts
	var counts RectCounts
	for _, r := range s.rects {
		switch r.Status {
		case "待派发":
			counts.Pending++
		case "整改中":
			counts.Ongoing++
		case "待复核":
			counts.Review++
		case "已闭环":
			counts.Closed++
		}
	}

	// Filter by tab
	statusFilter := statusFromTab(q.Tab)
	var filtered []*Rectification
	for _, r := range s.rects {
		if q.Tab != "" {
			if q.Tab == "ongoing" {
				// ongoing tab includes both 整改中 and 已超时
				if r.Status != "整改中" && r.Status != "已超时" {
					continue
				}
			} else if r.Status != statusFilter {
				continue
			}
		}
		filtered = append(filtered, r)
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

	var items []*Rectification
	if start < len(filtered) {
		items = filtered[start:end]
	} else {
		items = []*Rectification{}
	}

	return &ListResult{
		Items:    items,
		Total:    total,
		Page:     q.Page,
		PageSize: q.PageSize,
		Counts:   counts,
	}, nil
}

// Get retrieves a single rectification with its timeline and photos.
func (s *Service) Get(id int64) (*GetDetailResult, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	r, ok := s.rects[id]
	if !ok {
		return nil, errors.New("整改单不存在")
	}

	timeline := s.timelines[id]
	if timeline == nil {
		timeline = []Timeline{}
	}
	photos := s.photos[id]
	if photos == nil {
		photos = []Photo{}
	}

	return &GetDetailResult{
		Rectification: r,
		Timeline:      timeline,
		Photos:        photos,
	}, nil
}

// Dispatch transitions a rectification from 待派发 to 整改中.
func (s *Service) Dispatch(id int64) (*Rectification, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	r, ok := s.rects[id]
	if !ok {
		return nil, errors.New("整改单不存在")
	}

	if r.Status != "待派发" {
		return nil, errors.New("当前状态不可派发，仅待派发状态的整改单可派发")
	}

	now := time.Now().Format("2006-01-02 15:04:05")
	r.Status = "整改中"
	r.StatusType = "primary"
	r.UpdatedAt = now

	// Add timeline entry
	tl := Timeline{
		ID:        s.nextTimelineID,
		RectID:    r.ID,
		Action:    "任务派发",
		Operator:  "系统",
		Comment:   "已派发给" + r.Assignee,
		Timestamp: now,
	}
	s.timelines[r.ID] = append(s.timelines[r.ID], tl)
	s.nextTimelineID++

	log.Info().Int64("id", id).Str("assignee", r.Assignee).Msg("rectification dispatched")
	return r, nil
}

// SubmitFix transitions a rectification from 整改中 to 待复核.
func (s *Service) SubmitFix(id int64, comment string) (*Rectification, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	r, ok := s.rects[id]
	if !ok {
		return nil, errors.New("整改单不存在")
	}

	if r.Status != "整改中" {
		return nil, errors.New("当前状态不可提交整改，仅整改中的整改单可提交")
	}

	now := time.Now().Format("2006-01-02 15:04:05")
	r.Status = "待复核"
	r.StatusType = "info"
	r.UpdatedAt = now

	// Add timeline entry
	tl := Timeline{
		ID:        s.nextTimelineID,
		RectID:    r.ID,
		Action:    "整改汇报",
		Operator:  r.Assignee,
		Comment:   comment,
		Timestamp: now,
	}
	s.timelines[r.ID] = append(s.timelines[r.ID], tl)
	s.nextTimelineID++

	log.Info().Int64("id", id).Msg("rectification submitted for review")
	return r, nil
}

// ReviewRequest is the JSON body for reviewing a rectification.
type ReviewRequest struct {
	Approved bool   `json:"approved"`
	Comment  string `json:"comment"`
}

// Review transitions a rectification from 待复核 to 已闭环 (approved) or 整改中 (rejected).
func (s *Service) Review(id int64, req *ReviewRequest) (*Rectification, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	r, ok := s.rects[id]
	if !ok {
		return nil, errors.New("整改单不存在")
	}

	if r.Status != "待复核" {
		return nil, errors.New("当前状态不可复核，仅待复核状态的整改单可复核")
	}

	now := time.Now().Format("2006-01-02 15:04:05")

	if req.Approved {
		r.Status = "已闭环"
		r.StatusType = "success"
		r.UpdatedAt = now

		// Add approved timeline entry
		tl := Timeline{
			ID:        s.nextTimelineID,
			RectID:    r.ID,
			Action:    "复核通过",
			Operator:  "审核人",
			Comment:   req.Comment,
			Timestamp: now,
		}
		s.timelines[r.ID] = append(s.timelines[r.ID], tl)
		s.nextTimelineID++

		// Add closure entry
		tl2 := Timeline{
			ID:        s.nextTimelineID,
			RectID:    r.ID,
			Action:    "闭环确认",
			Operator:  "系统",
			Comment:   "隐患整改完成，已闭环归档",
			Timestamp: now,
		}
		s.timelines[r.ID] = append(s.timelines[r.ID], tl2)
		s.nextTimelineID++

		log.Info().Int64("id", id).Msg("rectification approved and closed")
	} else {
		r.Status = "整改中"
		r.StatusType = "primary"
		r.UpdatedAt = now

		// Add rejected timeline entry
		tl := Timeline{
			ID:        s.nextTimelineID,
			RectID:    r.ID,
			Action:    "复核驳回",
			Operator:  "审核人",
			Comment:   req.Comment,
			Timestamp: now,
		}
		s.timelines[r.ID] = append(s.timelines[r.ID], tl)
		s.nextTimelineID++

		log.Info().Int64("id", id).Msg("rectification review rejected, back to ongoing")
	}

	return r, nil
}

// UploadPhoto attaches a photo to a rectification.
func (s *Service) UploadPhoto(id int64, photoType, url string) (*Photo, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	r, ok := s.rects[id]
	if !ok {
		return nil, errors.New("整改单不存在")
	}
	_ = r // rectification exists, we just need to attach photo

	now := time.Now().Format("2006-01-02 15:04:05")
	photo := Photo{
		ID:      s.nextPhotoID,
		RectID:  id,
		Type:    photoType,
		URL:     url,
		TakenAt: now,
	}
	s.photos[id] = append(s.photos[id], photo)
	s.nextPhotoID++

	log.Info().Int64("id", id).Str("type", photoType).Msg("photo uploaded for rectification")
	return &photo, nil
}
