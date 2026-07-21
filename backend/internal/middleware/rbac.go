package middleware

import (
	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/pkg/response"
)

// RequirePermission returns a Gin middleware that checks if the authenticated
// user's role has the required resource:action permission.
// It must be used after AuthMiddleware so that "role" is set in the context.
func RequirePermission(resource string, action string) gin.HandlerFunc {
	return func(c *gin.Context) {
		role, exists := c.Get("role")
		if !exists {
			response.Unauthorized(c, "未授权访问")
			c.Abort()
			return
		}

		roleStr, ok := role.(string)
		if !ok {
			response.Forbidden(c, "无效的角色信息")
			c.Abort()
			return
		}

		if !HasPermission(roleStr, resource, action) {
			response.Forbidden(c, "权限不足，无法执行此操作")
			c.Abort()
			return
		}

		c.Next()
	}
}
