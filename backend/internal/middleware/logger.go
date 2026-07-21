package middleware

import (
	"time"

	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
)

// Logger returns a Gin middleware that logs every HTTP request using zerolog.
func Logger() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		path := c.Request.URL.Path
		raw := c.Request.URL.RawQuery

		// Process request
		c.Next()

		latency := time.Since(start)
		clientIP := c.ClientIP()
		method := c.Request.Method
		statusCode := c.Writer.Status()
		errorMessage := c.Errors.ByType(gin.ErrorTypePrivate).String()

		event := log.WithLevel(zerolog.InfoLevel)

		switch {
		case statusCode >= 500:
			event = log.WithLevel(zerolog.ErrorLevel)
		case statusCode >= 400:
			event = log.WithLevel(zerolog.WarnLevel)
		}

		event.Str("ip", clientIP).
			Str("method", method).
			Str("path", path).
			Int("status", statusCode).
			Dur("latency", latency).
			Str("error", errorMessage)

		if raw != "" {
			event.Str("query", raw)
		}

		event.Msg("request")
	}
}
