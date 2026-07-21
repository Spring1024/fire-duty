package middleware

import (
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"

	"github.com/spring1024/fire-duty/pkg/response"
)

// AuthMiddleware validates the Bearer JWT token and injects user info into context.
// It sets "userID" (float64), "username" (string), "role" (string), "permissions" ([]interface{}).
func AuthMiddleware(jwtSecret string) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			response.Unauthorized(c, "缺少 Authorization 头")
			c.Abort()
			return
		}

		parts := strings.SplitN(authHeader, " ", 2)
		if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
			response.Unauthorized(c, "Authorization 格式错误，应为 Bearer <token>")
			c.Abort()
			return
		}

		tokenStr := parts[1]

		// Parse and validate the token
		token, err := jwt.Parse(tokenStr, func(token *jwt.Token) (interface{}, error) {
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, jwt.ErrSignatureInvalid
			}
			return []byte(jwtSecret), nil
		})
		if err != nil {
			response.Unauthorized(c, "Token 无效或已过期")
			c.Abort()
			return
		}

		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok || !token.Valid {
			response.Unauthorized(c, "Token 无效")
			c.Abort()
			return
		}

		// Extract claims and inject into context
		if userID, exists := claims["userID"]; exists {
			c.Set("userID", userID)
		}
		if username, exists := claims["username"]; exists {
			c.Set("username", username)
		}
		if role, exists := claims["role"]; exists {
			c.Set("role", role)
		}
		if permissions, exists := claims["permissions"]; exists {
			c.Set("permissions", permissions)
		}

		c.Next()
	}
}

// GetUserIDFromContext extracts the userID from the Gin context.
// Returns 0 if not found or invalid.
func GetUserIDFromContext(c *gin.Context) int {
	raw, exists := c.Get("userID")
	if !exists {
		return 0
	}
	// JSON numbers are decoded as float64 by default
	switch v := raw.(type) {
	case float64:
		return int(v)
	case int:
		return v
	case int64:
		return int(v)
	default:
		return 0
	}
}

// GetRoleFromContext extracts the role string from the Gin context.
func GetRoleFromContext(c *gin.Context) string {
	raw, exists := c.Get("role")
	if !exists {
		return ""
	}
	s, _ := raw.(string)
	return s
}

// GetUsernameFromContext extracts the username string from the Gin context.
func GetUsernameFromContext(c *gin.Context) string {
	raw, exists := c.Get("username")
	if !exists {
		return ""
	}
	s, _ := raw.(string)
	return s
}
