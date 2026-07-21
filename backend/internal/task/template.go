package task

import "github.com/rs/zerolog/log"

// Template represents an inspection task template.
type Template struct {
	ID         int64          `json:"id"`
	Name       string         `json:"name"`
	DeviceType string         `json:"deviceType"`
	ItemCount  int            `json:"itemCount"`
	Cycle      string         `json:"cycle"` // 每月|每季度|每年
	Items      []TemplateItem `json:"items"`
}

// TemplateItem represents a single check item in a template.
type TemplateItem struct {
	ID      int64    `json:"id"`
	Name    string   `json:"name"`
	Type    string   `json:"type"` // boolean|enum|text
	Options []string `json:"options,omitempty"`
}

// seedTemplates populates the in-memory store with initial template data.
func (s *Service) seedTemplates() {
	seed := []Template{
		{
			Name:       "月度灭火器检查表",
			DeviceType: "灭火器",
			Cycle:      "每月",
			Items: []TemplateItem{
				{Name: "灭火器外观是否完好", Type: "boolean"},
				{Name: "压力表指针是否在绿色区域", Type: "boolean"},
				{Name: "保险销是否完好", Type: "boolean"},
				{Name: "喷管是否老化或堵塞", Type: "boolean"},
				{Name: "标识标签是否清晰", Type: "boolean"},
				{Name: "放置位置是否被遮挡", Type: "boolean"},
			},
		},
		{
			Name:       "月度消火栓检查表",
			DeviceType: "消火栓",
			Cycle:      "每月",
			Items: []TemplateItem{
				{Name: "消火栓箱门是否完好", Type: "boolean"},
				{Name: "水带是否齐全、无破损", Type: "boolean"},
				{Name: "水枪是否完好", Type: "boolean"},
				{Name: "阀门是否灵活无渗漏", Type: "boolean"},
				{Name: "栓口压力是否正常", Type: "enum", Options: []string{"正常", "偏低", "偏高"}},
				{Name: "消防软管盘卷是否整齐", Type: "boolean"},
				{Name: "报警按钮是否完好", Type: "boolean"},
				{Name: "外观标识是否清晰", Type: "boolean"},
			},
		},
		{
			Name:       "季度烟感检测表",
			DeviceType: "烟感探测器",
			Cycle:      "每季度",
			Items: []TemplateItem{
				{Name: "探测器外观是否完好", Type: "boolean"},
				{Name: "指示灯是否正常闪烁", Type: "boolean"},
				{Name: "防尘罩是否已取下", Type: "boolean"},
				{Name: "报警功能测试是否正常", Type: "enum", Options: []string{"正常", "异常", "需维修"}},
			},
		},
		{
			Name:       "季度喷淋检测表",
			DeviceType: "喷淋系统",
			Cycle:      "每季度",
			Items: []TemplateItem{
				{Name: "喷淋头外观是否完好", Type: "boolean"},
				{Name: "喷淋头是否有涂覆物", Type: "boolean"},
				{Name: "末端试水压力是否正常", Type: "enum", Options: []string{"正常", "偏低", "偏高"}},
				{Name: "信号阀是否处于开启状态", Type: "boolean"},
				{Name: "管道是否有渗漏", Type: "boolean"},
			},
		},
		{
			Name:       "年度消防联动测试表",
			DeviceType: "消防系统",
			Cycle:      "每年",
			Items: []TemplateItem{
				{Name: "火灾报警控制器是否正常", Type: "boolean"},
				{Name: "消防广播系统是否正常", Type: "boolean"},
				{Name: "消防电话系统是否正常", Type: "boolean"},
				{Name: "应急照明和疏散指示是否正常", Type: "boolean"},
				{Name: "防火门是否正常启闭", Type: "boolean"},
				{Name: "防火卷帘是否正常升降", Type: "boolean"},
				{Name: "消防水泵是否正常启动", Type: "boolean"},
				{Name: "喷淋泵是否正常启动", Type: "boolean"},
				{Name: "防排烟风机是否正常启动", Type: "boolean"},
				{Name: "消防电梯是否迫降", Type: "boolean"},
				{Name: "非消防电源是否切断", Type: "boolean"},
				{Name: "燃气切断阀是否动作", Type: "boolean"},
			},
		},
	}

	for i := range seed {
		seed[i].ID = s.nextTemplateID
		seed[i].ItemCount = len(seed[i].Items)
		for j := range seed[i].Items {
			seed[i].Items[j].ID = int64(j + 1)
		}
		s.templates[s.nextTemplateID] = &seed[i]
		s.nextTemplateID++
	}

	log.Info().Int("count", len(seed)).Msg("seeded in-memory templates")
}
