package main

import (
	"fmt"
	"log"

	"github.com/spring1024/fire-duty/config"
	"github.com/spring1024/fire-duty/internal/auth"
	"github.com/spring1024/fire-duty/internal/device"
	"github.com/spring1024/fire-duty/internal/grid"
	"github.com/spring1024/fire-duty/internal/mobile"
	"github.com/spring1024/fire-duty/internal/rectification"
	"github.com/spring1024/fire-duty/internal/router"
	"github.com/spring1024/fire-duty/internal/statistics"
	"github.com/spring1024/fire-duty/internal/task"
	"github.com/spring1024/fire-duty/internal/user"
)

func main() {
	// Load configuration
	cfg, err := config.Load("config/config.yaml")
	if err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	// Initialize database (stub — actual connection will be added later)
	_ = cfg.Database.DSN()

	// Initialize auth module with in-memory user store
	authSvc := auth.NewService(nil, cfg.JWT.Secret)
	authHandler := auth.NewHandler(authSvc)

	// Initialize device module with in-memory store
	deviceSvc := device.NewService()
	deviceHandler := device.NewHandler(deviceSvc)

	// Initialize task module with in-memory store
	taskSvc := task.NewService()
	taskHandler := task.NewHandler(taskSvc)

	// Initialize statistics module (pure computation, no storage)
	statsHandler := statistics.NewHandler()

	// Initialize rectification module with in-memory store
	rectSvc := rectification.NewService()
	rectHandler := rectification.NewHandler(rectSvc)

	// Initialize user module with in-memory store
	userSvc := user.NewService()
	userHandler := user.NewHandler(userSvc)

	// Initialize grid module with in-memory store
	gridSvc := grid.NewService()
	gridHandler := grid.NewHandler(gridSvc)

	// Initialize mobile module for offline sync / scan-check / photo upload
	mobileSvc := mobile.NewService(deviceSvc, taskSvc, rectSvc, cfg.Upload.Dir)
	mobileHandler := mobile.NewHandler(mobileSvc)

	// Setup router and pass handlers
	r := router.SetupRouter(authHandler, deviceHandler, taskHandler, statsHandler, rectHandler, userHandler, gridHandler, mobileHandler, cfg.JWT.Secret)

	// Start server
	addr := fmt.Sprintf(":%d", cfg.Server.Port)
	log.Printf("server starting on %s", addr)
	if err := r.Run(addr); err != nil {
		log.Fatalf("failed to start server: %v", err)
	}
}
