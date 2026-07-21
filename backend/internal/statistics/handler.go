package statistics

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/pkg/response"
)

// Handler provides pure-computation statistics APIs backed by in-memory preset data.
type Handler struct{}

// NewHandler creates a new Handler.
func NewHandler() *Handler {
	return &Handler{}
}

// DashboardStatsItem represents a single dashboard stat card.
type DashboardStatsItem struct {
	TotalDevices            int     `json:"totalDevices"`
	OnlineRate              float64 `json:"onlineRate"`
	TodayInspections        int     `json:"todayInspections"`
	PendingRectifications   int     `json:"pendingRectifications"`
	CompletionRate          float64 `json:"completionRate"`
	PlannedTasks            int     `json:"plannedTasks"`
	CompletedTasks          int     `json:"completedTasks"`
	OverdueTasks            int     `json:"overdueTasks"`
}

// DashboardAlert represents a single alert on the dashboard.
type DashboardAlert struct {
	Severity    string `json:"severity"`
	Description string `json:"description"`
}

// ComplianceData represents monthly compliance rates.
type ComplianceData struct {
	Months []string  `json:"months"`
	Rates  []float64 `json:"rates"`
}

// HazardItem represents a hazard type distribution entry.
type HazardItem struct {
	Type  string  `json:"type"`
	Rate  float64 `json:"rate"`
	Color string  `json:"color"`
}

// SummaryData represents the overall statistics summary.
type SummaryData struct {
	OverallComplianceRate float64 `json:"overallComplianceRate"`
	TopHazardType         string  `json:"topHazardType"`
	TopHazardRate         float64 `json:"topHazardRate"`
}

// ExportData is the full export payload.
type ExportData struct {
	Stats              DashboardStatsItem `json:"stats"`
	Alerts             []DashboardAlert   `json:"alerts"`
	Compliance         ComplianceData     `json:"compliance"`
	HazardDistribution []HazardItem       `json:"hazardDistribution"`
	Summary            SummaryData        `json:"summary"`
}

// dashboardStats returns the preset dashboard stats (matches frontend Dashboard.vue).
func dashboardStats() DashboardStatsItem {
	return DashboardStatsItem{
		TotalDevices:          568,
		OnlineRate:            93.7,
		TodayInspections:      45,
		PendingRectifications: 8,
		CompletionRate:        71.1,
		PlannedTasks:          63,
		CompletedTasks:        45,
		OverdueTasks:          18,
	}
}

// dashboardAlerts returns the preset alert list (matches frontend Dashboard.vue).
func dashboardAlerts() []DashboardAlert {
	return []DashboardAlert{
		{Severity: "P0", Description: "A区3号楼消防栓水压异常"},
		{Severity: "P1", Description: "B区2层烟感探测器离线"},
		{Severity: "P2", Description: "C区5号灭火器即将过期"},
		{Severity: "P1", Description: "D区1层安全通道堵塞"},
		{Severity: "P2", Description: "E区4号楼疏散指示故障"},
	}
}

// complianceData returns the preset monthly compliance rates (matches frontend Statistics.vue).
func complianceData() ComplianceData {
	return ComplianceData{
		Months: []string{"5月", "6月", "7月", "8月", "9月"},
		Rates:  []float64{60, 70, 85, 80, 88},
	}
}

// hazardDistribution returns the preset hazard type distribution (matches frontend Statistics.vue).
func hazardDistribution() []HazardItem {
	return []HazardItem{
		{Type: "灭火器", Rate: 35.0, Color: "#f56c6c"},
		{Type: "消火栓", Rate: 25.0, Color: "#e6a23c"},
		{Type: "疏散通道", Rate: 20.0, Color: "#409eff"},
		{Type: "报警系统", Rate: 12.0, Color: "#67c23a"},
		{Type: "其他", Rate: 8.0, Color: "#909399"},
	}
}

// summaryData returns the preset summary (matches frontend Statistics.vue).
func summaryData() SummaryData {
	return SummaryData{
		OverallComplianceRate: 93.5,
		TopHazardType:         "灭火器",
		TopHazardRate:         35.0,
	}
}

// DashboardStats handles GET /dashboard/stats
func (h *Handler) DashboardStats(c *gin.Context) {
	response.Success(c, dashboardStats())
}

// DashboardAlerts handles GET /dashboard/alerts
func (h *Handler) DashboardAlerts(c *gin.Context) {
	response.Success(c, dashboardAlerts())
}

// Compliance handles GET /statistics/compliance?months=6
func (h *Handler) Compliance(c *gin.Context) {
	// months query param is accepted but ignored for preset data
	response.Success(c, complianceData())
}

// HazardDistribution handles GET /statistics/hazard-distribution
func (h *Handler) HazardDistribution(c *gin.Context) {
	response.Success(c, hazardDistribution())
}

// Summary handles GET /statistics/summary
func (h *Handler) Summary(c *gin.Context) {
	response.Success(c, summaryData())
}

// Export handles GET /statistics/export (CSV-style JSON dump)
func (h *Handler) Export(c *gin.Context) {
	data := ExportData{
		Stats:              dashboardStats(),
		Alerts:             dashboardAlerts(),
		Compliance:         complianceData(),
		HazardDistribution: hazardDistribution(),
		Summary:            summaryData(),
	}
	c.JSON(http.StatusOK, gin.H{
		"code":    0,
		"message": "success",
		"data":    data,
	})
}
