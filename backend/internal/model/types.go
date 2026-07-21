package model

// Response is the unified JSON response format for all API responses.
type Response struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
}

// Standard error codes.
const (
	CodeSuccess       = 0
	CodeUnauthorized  = 401
	CodeForbidden     = 403
	CodeNotFound      = 404
	CodeValidationErr = 422
	CodeInternalErr   = 500
)
