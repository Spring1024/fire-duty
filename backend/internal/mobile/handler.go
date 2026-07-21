package mobile

import (
	"io"
	"strconv"

	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/internal/middleware"
	"github.com/spring1024/fire-duty/pkg/response"
)

// Handler handles HTTP requests for mobile endpoints.
type Handler struct {
	service *Service
}

// NewHandler creates a new mobile HTTP handler.
func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// Sync handles GET /api/v1/mobile/sync.
// Returns incremental data (devices, tasks, rectifications) since the given timestamp.
// Query params:
//   - since: ISO 8601 timestamp (optional). If omitted, all records are returned.
func (h *Handler) Sync(c *gin.Context) {
	since := c.Query("since")

	result, err := h.service.Sync(since)
	if err != nil {
		response.InternalError(c, "同步数据失败")
		return
	}

	response.Success(c, result)
}

// ScanCheck handles POST /api/v1/mobile/scan-check.
// Submits a quick scan check result from the mobile app.
// Body:
//
//	{
//	  "deviceCode": "EXT-001",
//	  "status": "正常",
//	  "remark": "压力正常",
//	  "imageData": "data:image/jpeg;base64,..."
//	}
func (h *Handler) ScanCheck(c *gin.Context) {
	var req ScanCheckRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请填写必填字段（deviceCode, status）")
		return
	}

	// Validate status
	if req.Status != "正常" && req.Status != "异常" {
		response.BadRequest(c, "status 字段必须为「正常」或「异常」")
		return
	}

	userID := middleware.GetUserIDFromContext(c)
	userName := middleware.GetUsernameFromContext(c)

	record, err := h.service.SaveScanCheck(&req, userID, userName)
	if err != nil {
		response.InternalError(c, "提交扫码检查结果失败")
		return
	}

	response.Created(c, record)
}

// UploadPhoto handles POST /api/v1/mobile/photo.
// Uploads a watermarked photo via multipart form.
// Form fields:
//   - image: the image file (required)
//   - deviceCode: device code for watermark (required)
//   - location: location info for watermark (required)
//   - inspector: inspector name for watermark (required)
func (h *Handler) UploadPhoto(c *gin.Context) {
	// Parse multipart file (max 10MB)
	if err := c.Request.ParseMultipartForm(10 << 20); err != nil {
		response.BadRequest(c, "上传文件大小不能超过10MB")
		return
	}

	file, header, err := c.Request.FormFile("image")
	if err != nil {
		response.BadRequest(c, "请上传图片文件（字段名: image）")
		return
	}
	defer file.Close()

	// Validate content type
	contentType := header.Header.Get("Content-Type")
	if contentType != "" {
		valid := contentType == "image/jpeg" ||
			contentType == "image/png" ||
			contentType == "image/gif" ||
			contentType == "image/webp"
		if !valid {
			response.BadRequest(c, "仅支持 JPEG、PNG、GIF、WebP 格式图片")
			return
		}
	}

	// Read the file into a buffer to check size (limit 10MB)
	limitedReader := io.LimitReader(file, 10<<20)
	fileBytes, err := io.ReadAll(limitedReader)
	if err != nil {
		response.InternalError(c, "读取文件失败")
		return
	}
	if len(fileBytes) >= 10<<20 {
		response.BadRequest(c, "图片大小不能超过10MB")
		return
	}

	deviceCode := c.PostForm("deviceCode")
	location := c.PostForm("location")
	inspector := c.PostForm("inspector")

	if deviceCode == "" || location == "" || inspector == "" {
		response.BadRequest(c, "请填写水印信息（deviceCode, location, inspector）")
		return
	}

	userID := middleware.GetUserIDFromContext(c)

	meta := &PhotoMetadata{
		DeviceCode: deviceCode,
		Location:   location,
		Inspector:  inspector,
	}

	// Write the file via a pipe to avoid keeping the full buffer in memory
	pr, pw := io.Pipe()
	go func() {
		pw.Write(fileBytes)
		pw.Close()
	}()

	record, err := h.service.SaveWatermarkPhoto(header.Filename, pr, meta, userID)
	if err != nil {
		response.InternalError(c, "上传照片失败: "+err.Error())
		return
	}

	response.Created(c, gin.H{
		"id":       record.ID,
		"filePath": record.FilePath,
		"takenAt":  record.TakenAt,
	})
}

// ListScanRecords handles GET /api/v1/mobile/scan-records.
// Returns stored scan check records for reference.
func (h *Handler) ListScanRecords(c *gin.Context) {
	// Parse pagination
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	if page < 1 {
		page = 1
	}
	if pageSize < 1 || pageSize > 100 {
		pageSize = 20
	}

	records := h.service.ListScanRecords(page, pageSize)
	response.Success(c, records)
}

// ListPhotos handles GET /api/v1/mobile/photos.
// Returns stored watermark photo records for reference.
func (h *Handler) ListPhotos(c *gin.Context) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	if page < 1 {
		page = 1
	}
	if pageSize < 1 || pageSize > 100 {
		pageSize = 20
	}

	photos := h.service.ListPhotos(page, pageSize)
	response.Success(c, photos)
}
