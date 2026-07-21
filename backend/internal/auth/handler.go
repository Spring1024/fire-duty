package auth

import (
	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/internal/middleware"
	"github.com/spring1024/fire-duty/pkg/response"
)

// Handler handles HTTP requests for authentication endpoints.
type Handler struct {
	svc *Service
}

// NewHandler creates a new auth HTTP handler.
func NewHandler(svc *Service) *Handler {
	return &Handler{svc: svc}
}

// loginRequest is the expected JSON body for POST /auth/login.
type loginRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

// Login handles user login.
func (h *Handler) Login(c *gin.Context) {
	var req loginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请输入用户名和密码")
		return
	}

	token, refreshToken, user, err := h.svc.Login(c.Request.Context(), req.Username, req.Password)
	if err != nil {
		response.Unauthorized(c, err.Error())
		return
	}

	response.Success(c, LoginResponse{
		Token:        token,
		RefreshToken: refreshToken,
		User:         user,
	})
}

// refreshTokenRequest is the expected JSON body for POST /auth/refresh.
type refreshTokenRequest struct {
	RefreshToken string `json:"refreshToken" binding:"required"`
}

// RefreshToken handles token refresh.
func (h *Handler) RefreshToken(c *gin.Context) {
	var req refreshTokenRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请输入 refreshToken")
		return
	}

	newToken, newRefresh, err := h.svc.RefreshToken(c.Request.Context(), req.RefreshToken)
	if err != nil {
		response.Unauthorized(c, err.Error())
		return
	}

	response.Success(c, TokenResponse{
		Token:        newToken,
		RefreshToken: newRefresh,
	})
}

// GetMe returns the current authenticated user's info.
func (h *Handler) GetMe(c *gin.Context) {
	userID := middleware.GetUserIDFromContext(c)
	if userID == 0 {
		response.Unauthorized(c, "无法获取用户信息")
		return
	}

	user, err := h.svc.GetUserByID(c.Request.Context(), userID)
	if err != nil {
		response.Unauthorized(c, err.Error())
		return
	}

	response.Success(c, user)
}

// changePasswordRequest is the expected JSON body for PUT /auth/password.
type changePasswordRequest struct {
	OldPassword string `json:"oldPassword" binding:"required"`
	NewPassword string `json:"newPassword" binding:"required"`
}

// ChangePassword handles password changes for authenticated users.
func (h *Handler) ChangePassword(c *gin.Context) {
	userID := middleware.GetUserIDFromContext(c)
	if userID == 0 {
		response.Unauthorized(c, "无法获取用户信息")
		return
	}

	var req changePasswordRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请提供原密码和新密码")
		return
	}

	if len(req.NewPassword) < 6 {
		response.BadRequest(c, "新密码长度不能少于6位")
		return
	}

	if err := h.svc.ChangePassword(c.Request.Context(), userID, req.OldPassword, req.NewPassword); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	c.JSON(200, gin.H{
		"code":    0,
		"message": "密码修改成功",
	})
}
