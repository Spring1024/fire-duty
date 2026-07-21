package device

import (
	"encoding/csv"
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/pkg/response"
)

// Handler handles HTTP requests for device endpoints.
type Handler struct {
	service *Service
}

// NewHandler creates a new device HTTP handler.
func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// List handles GET /devices with pagination and filters.
func (h *Handler) List(c *gin.Context) {
	var q ListQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		response.BadRequest(c, "查询参数格式错误")
		return
	}

	result, err := h.service.List(q)
	if err != nil {
		response.InternalError(c, "获取设备列表失败")
		return
	}

	response.Success(c, result)
}

// Get handles GET /devices/:id.
func (h *Handler) Get(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "设备ID格式错误")
		return
	}

	device, err := h.service.Get(id)
	if err != nil {
		response.NotFound(c, err.Error())
		return
	}

	response.Success(c, device)
}

// createRequest is the expected JSON body for POST /devices.
type createRequest struct {
	Code            string  `json:"code" binding:"required"`
	Name            string  `json:"name" binding:"required"`
	Type            string  `json:"type" binding:"required"`
	Status          string  `json:"status" binding:"required"`
	Location        string  `json:"location" binding:"required"`
	GridID          int64   `json:"gridId" binding:"required"`
	GridPath        string  `json:"gridPath" binding:"required"`
	Manufacturer    string  `json:"manufacturer"`
	InstallDate     string  `json:"installDate"`
	LastCheck       string  `json:"lastCheck"`
	LastMaintenance string  `json:"lastMaintenance"`
	Lat             float64 `json:"lat,omitempty"`
	Lng             float64 `json:"lng,omitempty"`
}

// Create handles POST /devices.
func (h *Handler) Create(c *gin.Context) {
	var req createRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请填写必填字段（code, name, type, status, location, gridId, gridPath）")
		return
	}

	device := &Device{
		Code:            req.Code,
		Name:            req.Name,
		Type:            req.Type,
		Status:          req.Status,
		Location:        req.Location,
		GridID:          req.GridID,
		GridPath:        req.GridPath,
		Manufacturer:    req.Manufacturer,
		InstallDate:     req.InstallDate,
		LastCheck:       req.LastCheck,
		LastMaintenance: req.LastMaintenance,
		Lat:             req.Lat,
		Lng:             req.Lng,
	}

	result, err := h.service.Create(device)
	if err != nil {
		response.InternalError(c, "创建设备失败")
		return
	}

	response.Created(c, result)
}

// updateRequest is the expected JSON body for PUT /devices/:id.
type updateRequest struct {
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
	Lat             float64 `json:"lat,omitempty"`
	Lng             float64 `json:"lng,omitempty"`
}

// Update handles PUT /devices/:id.
func (h *Handler) Update(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "设备ID格式错误")
		return
	}

	var req updateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请求体格式错误")
		return
	}

	device := &Device{
		Code:            req.Code,
		Name:            req.Name,
		Type:            req.Type,
		Status:          req.Status,
		Location:        req.Location,
		GridID:          req.GridID,
		GridPath:        req.GridPath,
		Manufacturer:    req.Manufacturer,
		InstallDate:     req.InstallDate,
		LastCheck:       req.LastCheck,
		LastMaintenance: req.LastMaintenance,
		Lat:             req.Lat,
		Lng:             req.Lng,
	}

	result, err := h.service.Update(id, device)
	if err != nil {
		response.NotFound(c, err.Error())
		return
	}

	response.Success(c, result)
}

// Delete handles DELETE /devices/:id.
func (h *Handler) Delete(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "设备ID格式错误")
		return
	}

	if err := h.service.Delete(id); err != nil {
		response.NotFound(c, err.Error())
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"code":    0,
		"message": "删除成功",
	})
}

// Tree handles GET /devices/tree.
func (h *Handler) Tree(c *gin.Context) {
	tree := h.service.Tree()
	response.Success(c, tree)
}

// importForm is the multipart form for POST /devices/import.
type importForm struct {
	File *DeviceImportFile `form:"file" binding:"required"`
}

// DeviceImportFile wraps the uploaded file metadata.
type DeviceImportFile struct {
	Filename string
	Content  []byte
}

// Import handles POST /devices/import (multipart CSV upload).
func (h *Handler) Import(c *gin.Context) {
	file, header, err := c.Request.FormFile("file")
	if err != nil {
		response.BadRequest(c, "请上传CSV文件")
		return
	}
	defer file.Close()

	// Validate extension
	if !strings.HasSuffix(strings.ToLower(header.Filename), ".csv") {
		response.BadRequest(c, "仅支持CSV格式文件")
		return
	}

	// Parse CSV
	reader := csv.NewReader(file)
	records, err := reader.ReadAll()
	if err != nil {
		response.BadRequest(c, "CSV文件格式错误")
		return
	}

	if len(records) < 2 {
		response.BadRequest(c, "CSV文件至少包含表头和一行数据")
		return
	}

	// Parse header row
	headerRow := records[0]
	colMap := make(map[string]int)
	for i, col := range headerRow {
		colMap[strings.TrimSpace(col)] = i
	}

	// Required columns
	requiredCols := []string{"code", "name", "type", "status", "location", "gridId", "gridPath"}
	for _, col := range requiredCols {
		if _, ok := colMap[col]; !ok {
			response.BadRequest(c, fmt.Sprintf("CSV缺少必填列: %s", col))
			return
		}
	}

	var devices []*Device
	for i := 1; i < len(records); i++ {
		row := records[i]
		get := func(col string) string {
			idx, ok := colMap[col]
			if !ok || idx >= len(row) {
				return ""
			}
			return strings.TrimSpace(row[idx])
		}

		getInt := func(col string) int64 {
			v := get(col)
			if v == "" {
				return 0
			}
			n, err := strconv.ParseInt(v, 10, 64)
			if err != nil {
				return 0
			}
			return n
		}

		getFloat := func(col string) float64 {
			v := get(col)
			if v == "" {
				return 0
			}
			n, err := strconv.ParseFloat(v, 64)
			if err != nil {
				return 0
			}
			return n
		}

		code := get("code")
		if code == "" {
			continue // Skip rows without code
		}

		devices = append(devices, &Device{
			Code:            code,
			Name:            get("name"),
			Type:            get("type"),
			Status:          get("status"),
			Location:        get("location"),
			GridID:          getInt("gridId"),
			GridPath:        get("gridPath"),
			Manufacturer:    get("manufacturer"),
			InstallDate:     get("installDate"),
			LastCheck:       get("lastCheck"),
			LastMaintenance: get("lastMaintenance"),
			Lat:             getFloat("lat"),
			Lng:             getFloat("lng"),
		})
	}

	if len(devices) == 0 {
		response.BadRequest(c, "CSV中没有有效的设备数据")
		return
	}

	imported, skipped, err := h.service.Import(devices)
	if err != nil {
		response.InternalError(c, "导入设备失败")
		return
	}

	response.Success(c, gin.H{
		"imported": imported,
		"skipped":  skipped,
		"total":    imported + skipped,
	})
}

// Export handles GET /devices/export, returning all devices as CSV.
func (h *Handler) Export(c *gin.Context) {
	devices := h.service.GetAll()

	c.Header("Content-Type", "text/csv; charset=utf-8")
	c.Header("Content-Disposition", "attachment; filename=devices_export.csv")

	// Write BOM for Excel compatibility
	c.Writer.Write([]byte{0xEF, 0xBB, 0xBF})

	writer := csv.NewWriter(c.Writer)
	// Header
	writer.Write([]string{
		"code", "name", "type", "status", "location",
		"gridId", "gridPath", "manufacturer", "installDate",
		"lastCheck", "lastMaintenance", "lat", "lng",
	})

	for _, d := range devices {
		writer.Write([]string{
			d.Code,
			d.Name,
			d.Type,
			d.Status,
			d.Location,
			strconv.FormatInt(d.GridID, 10),
			d.GridPath,
			d.Manufacturer,
			d.InstallDate,
			d.LastCheck,
			d.LastMaintenance,
			strconv.FormatFloat(d.Lat, 'f', 4, 64),
			strconv.FormatFloat(d.Lng, 'f', 4, 64),
		})
	}

	writer.Flush()
}
