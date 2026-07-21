package user

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/pkg/response"
)

// Handler handles HTTP requests for user endpoints.
type Handler struct {
	service *Service
}

// NewHandler creates a new user HTTP handler.
func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// List handles GET /users with pagination and filters.
func (h *Handler) List(c *gin.Context) {
	var q ListQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		response.BadRequest(c, "查询参数格式错误")
		return
	}

	result, err := h.service.List(q)
	if err != nil {
		response.InternalError(c, "获取用户列表失败")
		return
	}

	response.Success(c, result)
}

// Get handles GET /users/:id.
func (h *Handler) Get(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "用户ID格式错误")
		return
	}

	user, err := h.service.Get(id)
	if err != nil {
		response.NotFound(c, err.Error())
		return
	}

	response.Success(c, user)
}

// createRequest is the expected JSON body for POST /users.
type createRequest struct {
	Name     string `json:"name" binding:"required"`
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required,min=6"`
	Role     string `json:"role" binding:"required"`
	GridID   int64  `json:"gridId"`
	GridName string `json:"gridName"`
	Phone    string `json:"phone"`
}

// Create handles POST /users.
func (h *Handler) Create(c *gin.Context) {
	var req createRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请填写必填字段（name, username, password, role）")
		return
	}

	user := &User{
		Name:         req.Name,
		Username:     req.Username,
		PasswordHash: req.Password, // raw password, will be hashed by service
		Role:         req.Role,
		GridID:       req.GridID,
		GridName:     req.GridName,
		Phone:        req.Phone,
	}

	result, err := h.service.Create(user)
	if err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	response.Created(c, result)
}

// updateRequest is the expected JSON body for PUT /users/:id.
type updateRequest struct {
	Name     string `json:"name"`
	Username string `json:"username"`
	Password string `json:"password"`
	Role     string `json:"role"`
	GridID   *int64 `json:"gridId"`
	GridName string `json:"gridName"`
	Phone    string `json:"phone"`
	Status   string `json:"status"`
}

// Update handles PUT /users/:id.
func (h *Handler) Update(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "用户ID格式错误")
		return
	}

	var req updateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请求体格式错误")
		return
	}

	user := &User{
		Name:         req.Name,
		Username:     req.Username,
		PasswordHash: req.Password,
		Role:         req.Role,
		Phone:        req.Phone,
		Status:       req.Status,
	}
	if req.GridID != nil {
		user.GridID = *req.GridID
	}
	if req.GridName != "" {
		user.GridName = req.GridName
	}

	result, err := h.service.Update(id, user)
	if err != nil {
		if err.Error() == "用户不存在" {
			response.NotFound(c, err.Error())
		} else {
			response.BadRequest(c, err.Error())
		}
		return
	}

	response.Success(c, result)
}

// Delete handles DELETE /users/:id.
func (h *Handler) Delete(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "用户ID格式错误")
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
