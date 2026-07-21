package middleware

// RolePermissions defines which permissions each role has.
// Each permission is in the format "resource:action".
// "*" means all permissions.
var RolePermissions = map[string][]string{
	"超级管理员":   {"*"},
	"大网格负责人":  {"device:read", "device:write", "devices:read", "devices:write", "task:read", "task:write", "rectification:read", "rectification:write", "statistics:read"},
	"中网格组长":   {"device:read", "devices:read", "task:read", "rectification:read", "statistics:read"},
	"小网格检查员":  {"device:read", "devices:read", "task:read"},
}

// HasPermission checks if a role has a specific permission (resource:action).
func HasPermission(role string, resource string, action string) bool {
	perms, ok := RolePermissions[role]
	if !ok {
		return false
	}
	for _, p := range perms {
		if p == "*" {
			return true
		}
		if p == resource+":"+action {
			return true
		}
	}
	return false
}
