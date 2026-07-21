package task

import (
	"strconv"

	"github.com/gin-gonic/gin"

	"github.com/spring1024/fire-duty/pkg/response"
)

// Handler handles HTTP requests for task endpoints.
type Handler struct {
	service *Service
}

// NewHandler creates a new task HTTP handler.
func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// List handles GET /tasks with pagination and tab filtering.
func (h *Handler) List(c *gin.Context) {
	var q ListQuery
	if err := c.ShouldBindQuery(&q); err != nil {
		response.BadRequest(c, "查询参数格式错误")
		return
	}

	result, err := h.service.List(q)
	if err != nil {
		response.InternalError(c, "获取任务列表失败")
		return
	}

	response.Success(c, result)
}

// createTaskRequest is the expected JSON body for POST /tasks.
type createTaskRequest struct {
	DeviceCode   string `json:"deviceCode" binding:"required"`
	DeviceName   string `json:"deviceName" binding:"required"`
	DeviceType   string `json:"deviceType" binding:"required"`
	TemplateID   int64  `json:"templateId" binding:"required"`
	TemplateName string `json:"templateName"`
	Location     string `json:"location" binding:"required"`
	Assignee     string `json:"assignee" binding:"required"`
	AssigneeID   int64  `json:"assigneeId" binding:"required"`
	Deadline     string `json:"deadline" binding:"required"`
	Remark       string `json:"remark"`
}

// Create handles POST /tasks (dispatch a task).
func (h *Handler) Create(c *gin.Context) {
	var req createTaskRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请填写必填字段（deviceCode, deviceName, deviceType, templateId, location, assignee, assigneeId, deadline）")
		return
	}

	task := &Task{
		DeviceCode:   req.DeviceCode,
		DeviceName:   req.DeviceName,
		DeviceType:   req.DeviceType,
		TemplateID:   req.TemplateID,
		TemplateName: req.TemplateName,
		Location:     req.Location,
		Assignee:     req.Assignee,
		AssigneeID:   req.AssigneeID,
		Deadline:     req.Deadline,
		Remark:       req.Remark,
	}

	// Auto-fill template name if not provided
	if task.TemplateName == "" {
		if tmpl, err := h.service.GetTemplate(task.TemplateID); err == nil {
			task.TemplateName = tmpl.Name
		}
	}

	result, err := h.service.Create(task)
	if err != nil {
		response.InternalError(c, "创建任务失败")
		return
	}

	response.Created(c, result)
}

// Get handles GET /tasks/:id.
func (h *Handler) Get(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "任务ID格式错误")
		return
	}

	task, err := h.service.Get(id)
	if err != nil {
		response.NotFound(c, err.Error())
		return
	}

	response.Success(c, task)
}

// submitRequest is the expected JSON body for POST /tasks/:id/submit.
type submitRequest struct {
	Remark string `json:"remark"`
}

// Submit handles POST /tasks/:id/submit.
func (h *Handler) Submit(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		response.BadRequest(c, "任务ID格式错误")
		return
	}

	var req submitRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请求体格式错误")
		return
	}

	result, err := h.service.Submit(id, &SubmitRequest{Remark: req.Remark})
	if err != nil {
		if err.Error() == "任务不存在" {
			response.NotFound(c, err.Error())
		} else {
			response.BadRequest(c, err.Error())
		}
		return
	}

	response.Success(c, result)
}

// ListTemplates handles GET /tasks/templates.
func (h *Handler) ListTemplates(c *gin.Context) {
	templates := h.service.ListTemplates()
	response.Success(c, templates)
}

// createTemplateRequest is the expected JSON body for POST /tasks/templates.
type createTemplateRequest struct {
	Name       string                  `json:"name" binding:"required"`
	DeviceType string                  `json:"deviceType" binding:"required"`
	Cycle      string                  `json:"cycle" binding:"required"`
	Items      []createTemplateItemReq `json:"items" binding:"required,min=1"`
}

type createTemplateItemReq struct {
	Name    string   `json:"name" binding:"required"`
	Type    string   `json:"type" binding:"required"`
	Options []string `json:"options,omitempty"`
}

// CreateTemplate handles POST /tasks/templates.
func (h *Handler) CreateTemplate(c *gin.Context) {
	var req createTemplateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.BadRequest(c, "请填写必填字段（name, deviceType, cycle, items）")
		return
	}

	items := make([]TemplateItem, len(req.Items))
	for i, item := range req.Items {
		items[i] = TemplateItem{
			Name:    item.Name,
			Type:    item.Type,
			Options: item.Options,
		}
	}

	tmpl := &Template{
		Name:       req.Name,
		DeviceType: req.DeviceType,
		Cycle:      req.Cycle,
		Items:      items,
	}

	result, err := h.service.CreateTemplate(tmpl)
	if err != nil {
		response.InternalError(c, "创建模板失败")
		return
	}

	response.Created(c, result)
}
