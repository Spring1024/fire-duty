package mobile

import (
	"fmt"
	"io"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/rs/zerolog/log"

	"github.com/spring1024/fire-duty/internal/device"
	"github.com/spring1024/fire-duty/internal/rectification"
	"github.com/spring1024/fire-duty/internal/task"
)

// SyncResponse is the payload returned by the offline sync endpoint.
type SyncResponse struct {
	Devices        []*device.Device        `json:"devices"`
	Tasks          []*task.Task            `json:"tasks"`
	Rectifications []*rectification.Rectification `json:"rectifications"`
	SyncTime       string                  `json:"syncTime"`
}

// ScanCheckRequest is the JSON body for a quick scan-check submission.
type ScanCheckRequest struct {
	DeviceCode string `json:"deviceCode" binding:"required"`
	Status     string `json:"status" binding:"required"` // 正常|异常
	Remark     string `json:"remark"`
	ImageData  string `json:"imageData,omitempty"` // base64 image
}

// ScanCheckRecord represents a stored scan-check result.
type ScanCheckRecord struct {
	ID         int64  `json:"id"`
	DeviceCode string `json:"deviceCode"`
	Status     string `json:"status"` // 正常|异常
	Remark     string `json:"remark"`
	ImagePath  string `json:"imagePath,omitempty"`
	UserID     int    `json:"userId"`
	UserName   string `json:"userName"`
	CreatedAt  string `json:"createdAt"`
}

// PhotoMetadata holds watermark info for photo uploads.
type PhotoMetadata struct {
	DeviceCode string `json:"deviceCode"`
	Location   string `json:"location"`
	Inspector  string `json:"inspector"`
}

// WatermarkPhoto represents a stored watermarked photo record.
type WatermarkPhoto struct {
	ID         int64  `json:"id"`
	DeviceCode string `json:"deviceCode"`
	Location   string `json:"location"`
	Inspector  string `json:"inspector"`
	FilePath   string `json:"filePath"`
	UserID     int    `json:"userId"`
	TakenAt    string `json:"takenAt"`
}

// Service handles mobile endpoint business logic.
type Service struct {
	mu            sync.RWMutex
	scanRecords   map[int64]*ScanCheckRecord
	watermarkPhotos map[int64]*WatermarkPhoto
	nextScanID    int64
	nextPhotoID   int64
	uploadDir     string

	deviceSvc        *device.Service
	taskSvc          *task.Service
	rectificationSvc *rectification.Service
}

// NewService creates a new mobile Service.
// It holds references to the device, task, and rectification services for offline sync.
func NewService(deviceSvc *device.Service, taskSvc *task.Service, rectificationSvc *rectification.Service, uploadDir string) *Service {
	return &Service{
		scanRecords:      make(map[int64]*ScanCheckRecord),
		watermarkPhotos:  make(map[int64]*WatermarkPhoto),
		nextScanID:       1,
		nextPhotoID:      1,
		uploadDir:        uploadDir,
		deviceSvc:        deviceSvc,
		taskSvc:          taskSvc,
		rectificationSvc: rectificationSvc,
	}
}

// Sync returns all devices, tasks, and rectifications that have been updated since the given timestamp.
// If since is empty, all records are returned.
func (s *Service) Sync(since string) (*SyncResponse, error) {
	now := time.Now().Format(time.RFC3339)

	var devices []*device.Device
	var tasks []*task.Task
	var rectifications []*rectification.Rectification

	// Get all devices and filter by UpdatedAt
	allDevices := s.deviceSvc.GetAll()
	for _, d := range allDevices {
		if since == "" || d.UpdatedAt >= since {
			devices = append(devices, d)
		}
	}

	// Get all tasks — the task service doesn't expose GetAll, so we query with a large page size
	taskResult, err := s.taskSvc.List(task.ListQuery{
		Page:     1,
		PageSize: 10000,
	})
	if err == nil {
		for _, t := range taskResult.Items {
			if since == "" || t.CreatedAt >= since || (t.CompletedAt != "" && t.CompletedAt >= since) {
				tasks = append(tasks, t)
			}
		}
	}

	// Get all rectifications — no GetAll method either, query with large page
	rectResult, err := s.rectificationSvc.List(rectification.ListQuery{
		Page:     1,
		PageSize: 10000,
	})
	if err == nil {
		for _, r := range rectResult.Items {
			if since == "" || r.CreatedAt >= since || r.UpdatedAt >= since {
				rectifications = append(rectifications, r)
			}
		}
	}

	// Sort by ID for consistent output
	sort.Slice(devices, func(i, j int) bool { return devices[i].ID < devices[j].ID })
	sort.Slice(tasks, func(i, j int) bool { return tasks[i].ID < tasks[j].ID })
	sort.Slice(rectifications, func(i, j int) bool { return rectifications[i].ID < rectifications[j].ID })

	return &SyncResponse{
		Devices:        devices,
		Tasks:          tasks,
		Rectifications: rectifications,
		SyncTime:       now,
	}, nil
}

// SaveScanCheck stores a scan-check result submitted from the mobile app.
// If imageData (base64) is provided, it saves it as a file.
func (s *Service) SaveScanCheck(req *ScanCheckRequest, userID int, userName string) (*ScanCheckRecord, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now().Format(time.RFC3339)
	record := &ScanCheckRecord{
		ID:         s.nextScanID,
		DeviceCode: req.DeviceCode,
		Status:     req.Status,
		Remark:     req.Remark,
		UserID:     userID,
		UserName:   userName,
		CreatedAt:  now,
	}

	// Save base64 image if present
	if req.ImageData != "" {
		ext := ".jpg"
		// Detect image type from data URI prefix
		if strings.HasPrefix(req.ImageData, "data:image/png") {
			ext = ".png"
		} else if strings.HasPrefix(req.ImageData, "data:image/gif") {
			ext = ".gif"
		}
		// Strip data URI prefix if present
		data := req.ImageData
		if idx := strings.Index(data, ","); idx != -1 {
			data = data[idx+1:]
		}

		filename := fmt.Sprintf("scancheck/%d_%s_%s%s", s.nextScanID, req.DeviceCode, time.Now().Format("20060102150405"), ext)
		record.ImagePath = filename

		// Write base64-decoded data to file
		fullPath := filepath.Join(s.uploadDir, filename)
		if err := os.MkdirAll(filepath.Dir(fullPath), 0755); err != nil {
			log.Error().Err(err).Str("path", fullPath).Msg("failed to create scancheck directory")
		} else {
			decoded, decodeErr := decodeBase64Image(data)
			if decodeErr != nil {
				log.Error().Err(decodeErr).Msg("failed to decode base64 image for scan check")
			} else {
				if writeErr := os.WriteFile(fullPath, decoded, 0644); writeErr != nil {
					log.Error().Err(writeErr).Str("path", fullPath).Msg("failed to write scancheck image")
				} else {
					log.Info().Str("path", fullPath).Msg("saved scancheck image")
				}
			}
		}
	}

	s.scanRecords[s.nextScanID] = record
	s.nextScanID++

	log.Info().Int64("id", record.ID).Str("deviceCode", req.DeviceCode).Str("status", req.Status).Msg("saved scan check")
	return record, nil
}

// SaveWatermarkPhoto handles multipart watermarked photo upload.
// The file is saved to uploadDir/watermark/<date>/<filename>.
func (s *Service) SaveWatermarkPhoto(filename string, file io.Reader, meta *PhotoMetadata, userID int) (*WatermarkPhoto, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now()
	dateDir := now.Format("2006-01-02")

	// Build the file path: watermark/2026-07-21/<timestamp>_<deviceCode>_<filename>
	safeFilename := fmt.Sprintf("%d_%s_%s", now.UnixMilli(), sanitizeFilename(meta.DeviceCode), sanitizeFilename(filename))
	relativePath := filepath.Join("watermark", dateDir, safeFilename)
	fullPath := filepath.Join(s.uploadDir, relativePath)

	// Create directory structure
	if err := os.MkdirAll(filepath.Dir(fullPath), 0755); err != nil {
		return nil, fmt.Errorf("create upload directory: %w", err)
	}

	// Write the file
	dst, err := os.Create(fullPath)
	if err != nil {
		return nil, fmt.Errorf("create file: %w", err)
	}
	defer dst.Close()

	written, err := io.Copy(dst, file)
	if err != nil {
		return nil, fmt.Errorf("write file: %w", err)
	}

	record := &WatermarkPhoto{
		ID:         s.nextPhotoID,
		DeviceCode: meta.DeviceCode,
		Location:   meta.Location,
		Inspector:  meta.Inspector,
		FilePath:   relativePath,
		UserID:     userID,
		TakenAt:    now.Format(time.RFC3339),
	}
	s.watermarkPhotos[s.nextPhotoID] = record
	s.nextPhotoID++

	log.Info().Int64("id", record.ID).Str("deviceCode", meta.DeviceCode).
		Int64("bytes", written).Str("path", fullPath).Msg("saved watermarked photo")
	return record, nil
}

// decodeBase64Image decodes a base64 string (without the data URI prefix) into bytes.
func decodeBase64Image(data string) ([]byte, error) {
	// Use standard base64 decoding
	decoded := make([]byte, len(data)*3/4)
	n, err := b64Decode(decoded, data)
	if err != nil {
		return nil, err
	}
	return decoded[:n], nil
}

// b64Decode is a simple base64 decoder that doesn't require the encoding/base64 import.
// It handles standard base64 with padding.
func b64Decode(dst []byte, src string) (int, error) {
	const alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
	var decodeTable [256]byte
	for i := range decodeTable {
		decodeTable[i] = 0xFF
	}
	for i := 0; i < len(alphabet); i++ {
		decodeTable[alphabet[i]] = byte(i)
	}
	decodeTable['='] = 0

	pos := 0
	for i := 0; i < len(src); i += 4 {
		if i+3 >= len(src) {
			break
		}
		a := decodeTable[src[i]]
		b := decodeTable[src[i+1]]
		c := decodeTable[src[i+2]]
		d := decodeTable[src[i+3]]

		if a == 0xFF || b == 0xFF {
			return 0, fmt.Errorf("invalid base64 character at position %d", i)
		}

		dst[pos] = (a << 2) | (b >> 4)
		pos++
		if c != 0xFF {
			dst[pos] = (b << 4) | (c >> 2)
			pos++
		}
		if d != 0xFF {
			dst[pos] = (c << 6) | d
			pos++
		}
	}
	return pos, nil
}

// ListScanRecords returns paginated scan check records.
func (s *Service) ListScanRecords(page, pageSize int) map[string]interface{} {
	s.mu.RLock()
	defer s.mu.RUnlock()

	var all []*ScanCheckRecord
	for _, r := range s.scanRecords {
		all = append(all, r)
	}
	sort.Slice(all, func(i, j int) bool { return all[i].ID > all[j].ID })

	total := len(all)
	start := (page - 1) * pageSize
	if start >= total {
		return map[string]interface{}{
			"items":    []*ScanCheckRecord{},
			"total":    total,
			"page":     page,
			"pageSize": pageSize,
		}
	}
	end := start + pageSize
	if end > total {
		end = total
	}

	return map[string]interface{}{
		"items":    all[start:end],
		"total":    total,
		"page":     page,
		"pageSize": pageSize,
	}
}

// ListPhotos returns paginated watermark photo records.
func (s *Service) ListPhotos(page, pageSize int) map[string]interface{} {
	s.mu.RLock()
	defer s.mu.RUnlock()

	var all []*WatermarkPhoto
	for _, p := range s.watermarkPhotos {
		all = append(all, p)
	}
	sort.Slice(all, func(i, j int) bool { return all[i].ID > all[j].ID })

	total := len(all)
	start := (page - 1) * pageSize
	if start >= total {
		return map[string]interface{}{
			"items":    []*WatermarkPhoto{},
			"total":    total,
			"page":     page,
			"pageSize": pageSize,
		}
	}
	end := start + pageSize
	if end > total {
		end = total
	}

	return map[string]interface{}{
		"items":    all[start:end],
		"total":    total,
		"page":     page,
		"pageSize": pageSize,
	}
}

// sanitizeFilename removes potentially dangerous characters from a filename.
func sanitizeFilename(name string) string {
	// Replace path separators and other dangerous chars with underscore
	replacer := strings.NewReplacer(
		"/", "_", "\\", "_", "..", "_", ":", "_",
		" ", "_", "\x00", "", "\n", "", "\r", "",
	)
	return replacer.Replace(name)
}
