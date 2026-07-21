package router

import (
	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/internal/auth"
	"github.com/spring1024/fire-duty/internal/device"
	"github.com/spring1024/fire-duty/internal/grid"
	"github.com/spring1024/fire-duty/internal/middleware"
	"github.com/spring1024/fire-duty/internal/mobile"
	"github.com/spring1024/fire-duty/internal/rectification"
	"github.com/spring1024/fire-duty/internal/statistics"
	"github.com/spring1024/fire-duty/internal/task"
	"github.com/spring1024/fire-duty/internal/user"
)

// SetupRouter creates and configures the Gin engine with all route groups.
// Any handler may be nil (routes that require them are registered as no-ops).
func SetupRouter(authHandler *auth.Handler, deviceHandler *device.Handler, taskHandler *task.Handler, statsHandler *statistics.Handler, rectHandler *rectification.Handler, userHandler *user.Handler, gridHandler *grid.Handler, mobileHandler *mobile.Handler, jwtSecret string) *gin.Engine {
	r := gin.New()

	// Global middleware chain: Recovery → Logger → CORS
	r.Use(middleware.Recovery())
	r.Use(middleware.Logger())
	r.Use(middleware.CORS())

	// Health check
	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"code":    0,
			"message": "ok",
		})
	})

	// API v1 group
	v1 := r.Group("/api/v1")
	{
		// Auth module — authentication endpoints
		if authHandler != nil {
			authGroup := v1.Group("/auth")
			{
				// Public endpoints (no auth required)
				authGroup.POST("/login", authHandler.Login)
				authGroup.POST("/refresh", authHandler.RefreshToken)

				// Protected endpoints (auth required)
				authProtected := authGroup.Group("")
				authProtected.Use(middleware.AuthMiddleware(jwtSecret))
				{
					authProtected.GET("/me", authHandler.GetMe)
					authProtected.PUT("/password", authHandler.ChangePassword)
				}
			}
		}

		// Device module — CRUD + tree + import/export
		if deviceHandler != nil {
			deviceGroup := v1.Group("/devices")
			deviceGroup.Use(middleware.AuthMiddleware(jwtSecret))
			{
				deviceGroup.GET("", deviceHandler.List)
				deviceGroup.GET("/tree", deviceHandler.Tree)
				deviceGroup.GET("/:id", deviceHandler.Get)
				deviceGroup.POST("", middleware.RequirePermission("devices", "write"), deviceHandler.Create)
				deviceGroup.PUT("/:id", middleware.RequirePermission("devices", "write"), deviceHandler.Update)
				deviceGroup.DELETE("/:id", middleware.RequirePermission("devices", "write"), deviceHandler.Delete)
				deviceGroup.POST("/import", middleware.RequirePermission("devices", "write"), deviceHandler.Import)
				deviceGroup.GET("/export", deviceHandler.Export)
			}
		}

		// Task module — task dispatch, templates, inspection submission
		if taskHandler != nil {
			taskGroup := v1.Group("/tasks")
			taskGroup.Use(middleware.AuthMiddleware(jwtSecret))
			{
				taskGroup.GET("", taskHandler.List)
				taskGroup.POST("", middleware.RequirePermission("tasks", "write"), taskHandler.Create)
				taskGroup.GET("/templates", taskHandler.ListTemplates)
				taskGroup.POST("/templates", middleware.RequirePermission("tasks", "write"), taskHandler.CreateTemplate)
				taskGroup.GET("/:id", taskHandler.Get)
				taskGroup.POST("/:id/submit", taskHandler.Submit)
			}
		}

		// Rectification module — hazard rectification work orders
		if rectHandler != nil {
			rectGroup := v1.Group("/rectifications")
			rectGroup.Use(middleware.AuthMiddleware(jwtSecret))
			{
				rectGroup.GET("", rectHandler.List)
				rectGroup.GET("/:id", rectHandler.Get)
				rectGroup.PUT("/:id/dispatch", rectHandler.Dispatch)
				rectGroup.PUT("/:id/submit-fix", rectHandler.SubmitFix)
				rectGroup.PUT("/:id/review", rectHandler.Review)
				rectGroup.POST("/:id/photos", rectHandler.UploadPhoto)
			}
		}

		// Dashboard module — statistics dashboard
		if statsHandler != nil {
			dashboardGroup := v1.Group("/dashboard")
			dashboardGroup.Use(middleware.AuthMiddleware(jwtSecret))
			{
				dashboardGroup.GET("/stats", statsHandler.DashboardStats)
				dashboardGroup.GET("/alerts", statsHandler.DashboardAlerts)
			}

			statsGroup := v1.Group("/statistics")
			statsGroup.Use(middleware.AuthMiddleware(jwtSecret))
			{
				statsGroup.GET("/compliance", statsHandler.Compliance)
				statsGroup.GET("/hazard-distribution", statsHandler.HazardDistribution)
				statsGroup.GET("/summary", statsHandler.Summary)
				statsGroup.GET("/export", statsHandler.Export)
			}
		}

		// User module — user CRUD management
		if userHandler != nil {
			userGroup := v1.Group("/users")
			userGroup.Use(middleware.AuthMiddleware(jwtSecret))
			{
				userGroup.GET("", userHandler.List)
				userGroup.POST("", userHandler.Create)
				userGroup.GET("/:id", userHandler.Get)
				userGroup.PUT("/:id", userHandler.Update)
				userGroup.DELETE("/:id", userHandler.Delete)
			}
		}

		// Grid module — grid CRUD management with tree structure
		if gridHandler != nil {
			gridGroup := v1.Group("/grids")
			gridGroup.Use(middleware.AuthMiddleware(jwtSecret))
			{
				gridGroup.GET("", gridHandler.List)
				gridGroup.GET("/tree", gridHandler.Tree)
				gridGroup.POST("", gridHandler.Create)
				gridGroup.GET("/:id", gridHandler.Get)
				gridGroup.PUT("/:id", gridHandler.Update)
				gridGroup.DELETE("/:id", gridHandler.Delete)
			}
		}

		// Mobile module — offline sync, scan check, photo upload
		if mobileHandler != nil {
			mobileGroup := v1.Group("/mobile")
			mobileGroup.Use(middleware.AuthMiddleware(jwtSecret))
			{
				mobileGroup.GET("/sync", mobileHandler.Sync)
				mobileGroup.POST("/scan-check", mobileHandler.ScanCheck)
				mobileGroup.POST("/photo", mobileHandler.UploadPhoto)
				mobileGroup.GET("/scan-records", mobileHandler.ListScanRecords)
				mobileGroup.GET("/photos", mobileHandler.ListPhotos)
			}
		}
	}

	return r
}

// stubHandler is a placeholder handler for routes that are not yet implemented.
func stubHandler(c *gin.Context) {
	c.JSON(501, gin.H{
		"code":    501,
		"message": "not implemented yet",
	})
}
