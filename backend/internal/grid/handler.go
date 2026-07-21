package grid

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/pkg/response"
)

// Handler handles HTTP requests for grid endpoints.
type Handler struct {
	service *Service
}

// NewHandler creates a new grid HTTP handler.
func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// List handles GET /grids (returns flat list for management).
func (h *Handler) List(c *gin.Context) {
	result, err := h.service.List()
	if err != nil {
		response.InternalError(c, "获取网格列表失败")
		return
	}

	response.Success(c, result)
}

// Tree handles GET /grids/tree (returns tree structure).
func (h *Handler) Tree(c *gin.Context) {
	tree := h.service.Tree()
	response.Success(c, tree)
}

// Get handles GET /grids/:id.
func (h *Handler) Get(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "网格ID格式错误")
		return
	}

	grid, err := h.service.Get(id)
	if err != nil {
		response.NotFound(c, err.Error())
		return
	}

	response.Success(c, grid)
}

// createRequest is the expected JSON body for POST /grids.
type createRequest struct {
	Name        string `json:"name" binding:"required"`
	Level       string `json:"level"`
	ParentID    int64  `json:"parentId"`
	Leader      string `json:"leader"`
	DeviceCount int    `json:"deviceCount"`
	Contact     string `json:"contact"`
	Phone       string `json:"phone"`
	Scope       string `json:"scope"`
}

// Create handles POST /grids.
func (h *Handler) Create(c *gin.Context) {
	var req createRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请填写必填字段（name）")
		return
	}

	grid := &Grid{
		Name:        req.Name,
		Level:       req.Level,
		ParentID:    req.ParentID,
		Leader:      req.Leader,
		DeviceCount: req.DeviceCount,
		Contact:     req.Contact,
		Phone:       req.Phone,
		Scope:       req.Scope,
	}

	result, err := h.service.Create(grid)
	if err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	response.Created(c, result)
}

// updateRequest is the expected JSON body for PUT /grids/:id.
type updateRequest struct {
	Name        string `json:"name"`
	Level       string `json:"level"`
	ParentID    int64  `json:"parentId"`
	Leader      string `json:"leader"`
	DeviceCount int    `json:"deviceCount"`
	Contact     string `json:"contact"`
	Phone       string `json:"phone"`
	Scope       string `json:"scope"`
}

// Update handles PUT /grids/:id.
func (h *Handler) Update(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "网格ID格式错误")
		return
	}

	var req updateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请求体格式错误")
		return
	}

	grid := &Grid{
		Name:        req.Name,
		Level:       req.Level,
		ParentID:    req.ParentID,
		Leader:      req.Leader,
		DeviceCount: req.DeviceCount,
		Contact:     req.Contact,
		Phone:       req.Phone,
		Scope:       req.Scope,
	}

	result, err := h.service.Update(id, grid)
	if err != nil {
		if err.Error() == "网格不存在" {
			response.NotFound(c, err.Error())
		} else {
			response.BadRequest(c, err.Error())
		}
		return
	}

	response.Success(c, result)
}

// Delete handles DELETE /grids/:id.
func (h *Handler) Delete(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "网格ID格式错误")
		return
	}

	if err := h.service.Delete(id); err != nil {
		if err.Error() == "网格不存在" {
			response.NotFound(c, err.Error())
		} else {
			response.BadRequest(c, err.Error())
		}
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"code":    0,
		"message": "删除成功",
	})
}
