package device

import (
	"sort"
	"strings"
)

// TreeNode represents a node in the device location tree.
type TreeNode struct {
	ID       string      `json:"id"`
	Label    string      `json:"label"`
	Count    int         `json:"count"`
	Children []*TreeNode `json:"children,omitempty"`
}

// buildTree constructs a hierarchical tree from device grid paths.
// Grid paths follow the format "Building/Floor" (e.g., "A栋/3层").
// Returns the root-level building nodes with floor children.
func buildTree(devices []*Device) []*TreeNode {
	// buildingMap: buildingName -> { floorName -> count }
	buildingMap := make(map[string]map[string]int)
	// Track the floor index (e.g., "B1层" < "1层" < "2层")
	floorOrder := make(map[string]int)

	for _, d := range devices {
		parts := strings.SplitN(d.GridPath, "/", 2)
		if len(parts) < 2 {
			continue
		}
		building := strings.TrimSpace(parts[0])
		floor := strings.TrimSpace(parts[1])

		if _, ok := buildingMap[building]; !ok {
			buildingMap[building] = make(map[string]int)
		}
		buildingMap[building][floor]++

		// Assign floor order if not already set
		if _, ok := floorOrder[floor]; !ok {
			floorOrder[floor] = parseFloorOrder(floor)
		}
	}

	// Sort buildings
	var buildingNames []string
	for name := range buildingMap {
		buildingNames = append(buildingNames, name)
	}
	sort.Strings(buildingNames)

	var roots []*TreeNode
	for _, building := range buildingNames {
		floors := buildingMap[building]

		var floorNames []string
		for name := range floors {
			floorNames = append(floorNames, name)
		}
		sort.Slice(floorNames, func(i, j int) bool {
			return floorOrder[floorNames[i]] < floorOrder[floorNames[j]]
		})

		totalCount := 0
		var children []*TreeNode
		for _, floor := range floorNames {
			count := floors[floor]
			totalCount += count

			// Determine the building prefix for floor IDs
			prefix := buildingPrefix(building)

			children = append(children, &TreeNode{
				ID:    prefix + "-" + floor,
				Label: floor,
				Count: count,
			})
		}

		roots = append(roots, &TreeNode{
			ID:       buildingPrefix(building),
			Label:    building,
			Count:    totalCount,
			Children: children,
		})
	}

	return roots
}

// buildingPrefix returns a normalized ID prefix for a building name.
// E.g., "A栋" -> "building-a", "B1层" -> "b1"
func buildingPrefix(name string) string {
	name = strings.ToLower(name)
	name = strings.ReplaceAll(name, "栋", "")
	name = strings.ReplaceAll(name, " ", "-")
	return "building-" + name
}

// parseFloorOrder converts a floor label to a sortable integer.
// B1 -> -1, B2 -> -2, 1 -> 1, 2 -> 2, etc.
func parseFloorOrder(floor string) int {
	floor = strings.TrimSuffix(floor, "层")
	floor = strings.TrimSpace(floor)

	if strings.HasPrefix(floor, "B") || strings.HasPrefix(floor, "b") {
		num := 0
		for _, c := range floor[1:] {
			if c >= '0' && c <= '9' {
				num = num*10 + int(c-'0')
			}
		}
		if num == 0 {
			return -1 // B1 -> -1
		}
		return -num
	}

	num := 0
	for _, c := range floor {
		if c >= '0' && c <= '9' {
			num = num*10 + int(c-'0')
		}
	}
	if num == 0 {
		return 0 // Ground/底层
	}
	return num
}

// Tree returns the device location tree with device counts per node.
func (s *Service) Tree() []*TreeNode {
	s.mu.RLock()
	defer s.mu.RUnlock()

	devices := make([]*Device, 0, len(s.devices))
	for _, d := range s.devices {
		devices = append(devices, d)
	}

	return buildTree(devices)
}
