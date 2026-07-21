package rectification

import (
	"strconv"

	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/pkg/response"
)

// Handler handles HTTP requests for rectification endpoints.
type Handler struct {
	service *Service
}

// NewHandler creates a new rectification HTTP handler.
func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// List handles GET /rectifications with pagination and tab filtering.
func (h *Handler) List(c *gin.Context) {
	var q ListQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		response.BadRequest(c, "查询参数格式错误")
		return
	}

	result, err := h.service.List(q)
	if err != nil {
		response.InternalError(c, "获取整改单列表失败")
		return
	}

	response.Success(c, result)
}

// Get handles GET /rectifications/:id — returns rectification detail with timeline and photos.
func (h *Handler) Get(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "整改单ID格式错误")
		return
	}

	detail, err := h.service.Get(id)
	if err != nil {
		response.NotFound(c, err.Error())
		return
	}

	response.Success(c, detail)
}

// Dispatch handles PUT /rectifications/:id/dispatch — transitions from 待派发 to 整改中.
func (h *Handler) Dispatch(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "整改单ID格式错误")
		return
	}

	result, err := h.service.Dispatch(id)
	if err != nil {
		if err.Error() == "整改单不存在" {
			response.NotFound(c, err.Error())
		} else {
			response.BadRequest(c, err.Error())
		}
		return
	}

	response.Success(c, result)
}

// submitFixRequest is the expected JSON body for PUT /rectifications/:id/submit-fix.
type submitFixRequest struct {
	Comment string `json:"comment"`
}

// SubmitFix handles PUT /rectifications/:id/submit-fix — transitions from 整改中 to 待复核.
func (h *Handler) SubmitFix(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "整改单ID格式错误")
		return
	}

	var req submitFixRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请求体格式错误")
		return
	}

	result, err := h.service.SubmitFix(id, req.Comment)
	if err != nil {
		if err.Error() == "整改单不存在" {
			response.NotFound(c, err.Error())
		} else {
			response.BadRequest(c, err.Error())
		}
		return
	}

	response.Success(c, result)
}

// reviewRequest is the expected JSON body for PUT /rectifications/:id/review.
type reviewRequest struct {
	Approved bool   `json:"approved"`
	Comment  string `json:"comment"`
}

// Review handles PUT /rectifications/:id/review — transitions from 待复核 to 已闭环 or 整改中.
func (h *Handler) Review(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "整改单ID格式错误")
		return
	}

	var req reviewRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请提供 approved/comment 字段")
		return
	}

	result, err := h.service.Review(id, &ReviewRequest{
		Approved: req.Approved,
		Comment:  req.Comment,
	})
	if err != nil {
		if err.Error() == "整改单不存在" {
			response.NotFound(c, err.Error())
		} else {
			response.BadRequest(c, err.Error())
		}
		return
	}

	response.Success(c, result)
}

// uploadPhotoRequest is the expected JSON body for POST /rectifications/:id/photos.
type uploadPhotoRequest struct {
	Type string `json:"type" binding:"required"` // before,after
	URL  string `json:"url" binding:"required"`
}

// UploadPhoto handles POST /rectifications/:id/photos.
func (h *Handler) UploadPhoto(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "整改单ID格式错误")
		return
	}

	var req uploadPhotoRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请提供 type（before/after）和 url 字段")
		return
	}

	if req.Type != "before" && req.Type != "after" {
		response.BadRequest(c, "照片类型必须为 before 或 after")
		return
	}

	result, err := h.service.UploadPhoto(id, req.Type, req.URL)
	if err != nil {
		if err.Error() == "整改单不存在" {
			response.NotFound(c, err.Error())
		} else {
			response.InternalError(c, "上传照片失败")
		}
		return
	}

	response.Created(c, result)
}
