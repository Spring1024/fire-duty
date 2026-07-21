package response

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// Response is the unified JSON response envelope.
type Response struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
}

// Error codes shared with internal/model.
const (
	CodeSuccess       = 0
	CodeUnauthorized  = 401
	CodeForbidden     = 403
	CodeNotFound      = 404
	CodeValidationErr = 422
	CodeInternalErr   = 500
)

// Success sends a success response with optional data.
func Success(c *gin.Context, data interface{}) {
	c.JSON(http.StatusOK, Response{
		Code:    CodeSuccess,
		Message: "success",
		Data:    data,
	})
}

// Created sends a 201 created response.
func Created(c *gin.Context, data interface{}) {
	c.JSON(http.StatusCreated, Response{
		Code:    CodeSuccess,
		Message: "created",
		Data:    data,
	})
}

// Error sends an error response with the given HTTP status, error code, and message.
func Error(c *gin.Context, httpStatus int, code int, msg string) {
	c.AbortWithStatusJSON(httpStatus, Response{
		Code:    code,
		Message: msg,
	})
}

// BadRequest sends a 422 validation error.
func BadRequest(c *gin.Context, msg string) {
	Error(c, http.StatusUnprocessableEntity, CodeValidationErr, msg)
}

// Unauthorized sends a 401 error.
func Unauthorized(c *gin.Context, msg string) {
	Error(c, http.StatusUnauthorized, CodeUnauthorized, msg)
}

// Forbidden sends a 403 error.
func Forbidden(c *gin.Context, msg string) {
	Error(c, http.StatusForbidden, CodeForbidden, msg)
}

// NotFound sends a 404 error.
func NotFound(c *gin.Context, msg string) {
	Error(c, http.StatusNotFound, CodeNotFound, msg)
}

// InternalError sends a 500 error.
func InternalError(c *gin.Context, msg string) {
	Error(c, http.StatusInternalServerError, CodeInternalErr, msg)
}
