package device

import (
	"errors"
	"fmt"
	"math"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/rs/zerolog/log"
)

// Device represents a fire safety device.
type Device struct {
	ID              int64   `json:"id"`
	Code            string  `json:"code"`
	Name            string  `json:"name"`
	Type            string  `json:"type"`
	Status          string  `json:"status"`
	Location        string  `json:"location"`
	GridID          int64   `json:"gridId"`
	GridPath        string  `json:"gridPath"`
	Manufacturer    string  `json:"manufacturer"`
	InstallDate     string  `json:"installDate"`
	LastCheck       string  `json:"lastCheck"`
	LastMaintenance string  `json:"lastMaintenance"`
	QRCode          string  `json:"qrCode"`
	Lat             float64 `json:"lat,omitempty"`
	Lng             float64 `json:"lng,omitempty"`
	CreatedAt       string  `json:"createdAt"`
	UpdatedAt       string  `json:"updatedAt"`
}

// ListQuery holds pagination and filter parameters for listing devices.
type ListQuery struct {
	Page     int    `form:"page"`
	PageSize int    `form:"pageSize"`
	Search   string `form:"search"`
	Type     string `form:"type"`
	Status   string `form:"status"`
	GridID   int64  `form:"gridId"`
}

// ListResult is the paginated result of a device list query.
type ListResult struct {
	Items    []*Device `json:"items"`
	Total    int64     `json:"total"`
	Page     int       `json:"page"`
	PageSize int       `json:"pageSize"`
}

// Service handles all device business logic with an in-memory store.
// This can be swapped for a database-backed implementation later.
type Service struct {
	mu      sync.RWMutex
	devices map[int64]*Device
	nextID  int64
}

// NewService creates a new device Service with seed data.
func NewService() *Service {
	s := &Service{
		devices: make(map[int64]*Device),
		nextID:  1,
	}
	s.seed()
	return s
}

// seed populates the in-memory store with initial device data.
// Data matches the frontend Devicemanagement.vue mock data (8 devices).
func (s *Service) seed() {
	now := time.Now().Format(time.RFC3339)
	seed := []Device{
		{
			Code:            "EXT-001",
			Name:            "3层走廊灭火器",
			Type:            "灭火器",
			Status:          "正常",
			Location:        "A栋-3层走廊东侧",
			GridID:          5,
			GridPath:        "A栋/3层",
			Manufacturer:    "中消安",
			InstallDate:     "2024-01-15",
			LastCheck:       "2025-03-10",
			LastMaintenance: "2025-01-20",
			Lat:             30.1234,
			Lng:             120.5678,
		},
		{
			Code:            "HYD-008",
			Name:            "东侧消火栓",
			Type:            "消火栓",
			Status:          "正常",
			Location:        "A栋-1层东侧",
			GridID:          3,
			GridPath:        "A栋/1层",
			Manufacturer:    "天广",
			InstallDate:     "2023-06-20",
			LastCheck:       "2025-04-01",
			LastMaintenance: "2025-02-15",
			Lat:             30.1240,
			Lng:             120.5680,
		},
		{
			Code:            "SEN-023",
			Name:            "B栋烟感",
			Type:            "烟感探测器",
			Status:          "故障",
			Location:        "B栋-3层305室",
			GridID:          9,
			GridPath:        "B栋/3层",
			Manufacturer:    "海湾",
			InstallDate:     "2022-11-01",
			LastCheck:       "2025-04-05",
			LastMaintenance: "2025-01-10",
			Lat:             30.1250,
			Lng:             120.5690,
		},
		{
			Code:            "SPR-015",
			Name:            "地下车库喷淋",
			Type:            "喷淋系统",
			Status:          "正常",
			Location:        "B1层车库B区",
			GridID:          2,
			GridPath:        "A栋/B1层",
			Manufacturer:    "中消安",
			InstallDate:     "2023-03-10",
			LastCheck:       "2025-03-20",
			LastMaintenance: "2025-02-28",
			Lat:             30.1220,
			Lng:             120.5670,
		},
		{
			Code:            "EXT-003",
			Name:            "大厅灭火器",
			Type:            "灭火器",
			Status:          "正常",
			Location:        "A栋-1层大厅",
			GridID:          3,
			GridPath:        "A栋/1层",
			Manufacturer:    "中消安",
			InstallDate:     "2024-01-15",
			LastCheck:       "2025-03-25",
			LastMaintenance: "2025-01-20",
			Lat:             30.1242,
			Lng:             120.5675,
		},
		{
			Code:            "HYD-012",
			Name:            "西侧消火栓",
			Type:            "消火栓",
			Status:          "正常",
			Location:        "C栋-2层西侧",
			GridID:          14,
			GridPath:        "C栋/2层",
			Manufacturer:    "天广",
			InstallDate:     "2023-06-20",
			LastCheck:       "2025-04-02",
			LastMaintenance: "2025-02-15",
			Lat:             30.1260,
			Lng:             120.5700,
		},
		{
			Code:            "SEN-031",
			Name:            "C栋温感",
			Type:            "温感探测器",
			Status:          "正常",
			Location:        "C栋-5层501室",
			GridID:          17,
			GridPath:        "C栋/5层",
			Manufacturer:    "海湾",
			InstallDate:     "2022-11-01",
			LastCheck:       "2025-04-05",
			LastMaintenance: "2025-01-10",
			Lat:             30.1270,
			Lng:             120.5710,
		},
		{
			Code:            "SPR-022",
			Name:            "A栋车库喷淋",
			Type:            "喷淋系统",
			Status:          "正常",
			Location:        "A栋-B1层车库A区",
			GridID:          2,
			GridPath:        "A栋/B1层",
			Manufacturer:    "中消安",
			InstallDate:     "2023-03-10",
			LastCheck:       "2025-03-18",
			LastMaintenance: "2025-02-28",
			Lat:             30.1225,
			Lng:             120.5673,
		},
	}

	for i := range seed {
		seed[i].ID = s.nextID
		seed[i].QRCode = fmt.Sprintf("FD-%s-%d", seed[i].Code, seed[i].ID)
		seed[i].CreatedAt = now
		seed[i].UpdatedAt = now
		s.devices[s.nextID] = &seed[i]
		s.nextID++
	}

	log.Info().Int("count", len(seed)).Msg("seeded in-memory devices")
}

// List returns a paginated, filtered list of devices.
func (s *Service) List(q ListQuery) (*ListResult, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if q.Page < 1 {
		q.Page = 1
	}
	if q.PageSize < 1 {
		q.PageSize = 20
	}

	var filtered []*Device
	for _, d := range s.devices {
		// Search filter (code or name)
		if q.Search != "" {
			search := strings.ToLower(q.Search)
			if !strings.Contains(strings.ToLower(d.Code), search) &&
				!strings.Contains(strings.ToLower(d.Name), search) {
				continue
			}
		}
		// Type filter
		if q.Type != "" && d.Type != q.Type {
			continue
		}
		// Status filter
		if q.Status != "" && d.Status != q.Status {
			continue
		}
		// GridID filter
		if q.GridID > 0 && d.GridID != q.GridID {
			continue
		}
		filtered = append(filtered, d)
	}

	// Sort by ID descending (newest first)
	sort.Slice(filtered, func(i, j int) bool {
		return filtered[i].ID > filtered[j].ID
	})

	total := int64(len(filtered))
	totalPages := int(math.Ceil(float64(total) / float64(q.PageSize)))
	if q.Page > totalPages {
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

	var items []*Device
	if start < len(filtered) {
		items = filtered[start:end]
	} else {
		items = []*Device{}
	}

	return &ListResult{
		Items:    items,
		Total:    total,
		Page:     q.Page,
		PageSize: q.PageSize,
	}, nil
}

// Get retrieves a single device by ID.
func (s *Service) Get(id int64) (*Device, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	d, ok := s.devices[id]
	if !ok {
		return nil, errors.New("设备不存在")
	}
	return d, nil
}

// Create adds a new device to the store.
func (s *Service) Create(d *Device) (*Device, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now().Format(time.RFC3339)
	d.ID = s.nextID
	d.QRCode = fmt.Sprintf("FD-%s-%d", d.Code, d.ID)
	d.CreatedAt = now
	d.UpdatedAt = now

	s.devices[d.ID] = d
	s.nextID++

	log.Info().Int64("id", d.ID).Str("code", d.Code).Msg("created device")
	return d, nil
}

// Update modifies an existing device.
func (s *Service) Update(id int64, updated *Device) (*Device, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	existing, ok := s.devices[id]
	if !ok {
		return nil, errors.New("设备不存在")
	}

	now := time.Now().Format(time.RFC3339)

	if updated.Code != "" {
		existing.Code = updated.Code
	}
	if updated.Name != "" {
		existing.Name = updated.Name
	}
	if updated.Type != "" {
		existing.Type = updated.Type
	}
	if updated.Status != "" {
		existing.Status = updated.Status
	}
	if updated.Location != "" {
		existing.Location = updated.Location
	}
	if updated.GridID > 0 {
		existing.GridID = updated.GridID
	}
	if updated.GridPath != "" {
		existing.GridPath = updated.GridPath
	}
	if updated.Manufacturer != "" {
		existing.Manufacturer = updated.Manufacturer
	}
	if updated.InstallDate != "" {
		existing.InstallDate = updated.InstallDate
	}
	if updated.LastCheck != "" {
		existing.LastCheck = updated.LastCheck
	}
	if updated.LastMaintenance != "" {
		existing.LastMaintenance = updated.LastMaintenance
	}
	if updated.Lat != 0 {
		existing.Lat = updated.Lat
	}
	if updated.Lng != 0 {
		existing.Lng = updated.Lng
	}
	existing.UpdatedAt = now

	log.Info().Int64("id", id).Msg("updated device")
	return existing, nil
}

// Delete removes a device from the store.
func (s *Service) Delete(id int64) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	if _, ok := s.devices[id]; !ok {
		return errors.New("设备不存在")
	}

	delete(s.devices, id)
	log.Info().Int64("id", id).Msg("deleted device")
	return nil
}

// GetAll returns all devices (for export).
func (s *Service) GetAll() []*Device {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]*Device, 0, len(s.devices))
	for _, d := range s.devices {
		result = append(result, d)
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].ID < result[j].ID
	})

	return result
}

// Import adds multiple devices at once, skipping duplicates by code.
func (s *Service) Import(devices []*Device) (int, int, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now().Format(time.RFC3339)
	imported := 0
	skipped := 0

	// Build set of existing codes
	existingCodes := make(map[string]bool)
	for _, d := range s.devices {
		existingCodes[d.Code] = true
	}

	for _, d := range devices {
		if existingCodes[d.Code] {
			skipped++
			continue
		}

		d.ID = s.nextID
		d.QRCode = fmt.Sprintf("FD-%s-%d", d.Code, d.ID)
		d.CreatedAt = now
		d.UpdatedAt = now

		s.devices[d.ID] = d
		s.nextID++
		existingCodes[d.Code] = true
		imported++
	}

	log.Info().Int("imported", imported).Int("skipped", skipped).Msg("imported devices")
	return imported, skipped, nil
}
