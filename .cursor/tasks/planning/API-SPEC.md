# API Specification

Base URL: `http://localhost:8765`

All requests and responses use JSON format unless otherwise specified.

## Health Check

### GET `/health`

Check if server is running.

**Response:**
```json
{
  "status": "ok",
  "version": "1.0.0",
  "uptime": 3600
}
```

---

## Tasks API

### GET `/tasks/list`

List all tasks in the project.

**Response:**
```json
{
  "tasks": [
    {
      "id": "work-change-type-diff",
      "summary": "Investigate work change type diff issue",
      "description": "Analyze Dec 16 incident with employee data",
      "created": "2025-01-01T10:00:00Z",
      "updated": "2025-01-01T15:30:00Z",
      "active": true,
      "project": "payroll-hub-data"
    },
    {
      "id": "refactor-employee-service",
      "summary": "Refactor EmployeeService",
      "active": false,
      "project": "payroll-hub-data"
    }
  ]
}
```

### GET `/tasks/current`

Get currently active task.

**Response:**
```json
{
  "task": {
    "id": "work-change-type-diff",
    "summary": "Investigate work change type diff issue",
    "active": true,
    "context": {
      "openFiles": [
        "/Users/avner/IdeaProjects/payroll-hub-data/src/main/kotlin/EmployeeService.kt",
        "/Users/avner/IdeaProjects/payroll-hub-data/src/test/kotlin/EmployeeServiceTest.kt"
      ],
      "activeFile": "/Users/avner/IdeaProjects/payroll-hub-data/src/main/kotlin/EmployeeService.kt",
      "cursorPosition": {
        "line": 42,
        "column": 15
      }
    }
  }
}
```

**Response (no active task):**
```json
{
  "task": null
}
```

### POST `/tasks/switch`

Switch to a different task (restores context).

**Request:**
```json
{
  "taskId": "refactor-employee-service"
}
```

**Response:**
```json
{
  "success": true,
  "previousTask": "work-change-type-diff",
  "currentTask": "refactor-employee-service",
  "contextRestored": true,
  "filesOpened": [
    "/Users/avner/IdeaProjects/payroll-hub-data/src/main/kotlin/EmployeeService.kt"
  ]
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "TASK_NOT_FOUND",
    "message": "Task with ID 'invalid-task' not found"
  }
}
```

### POST `/tasks/create`

Create a new task.

**Request:**
```json
{
  "summary": "Add employee validation logic",
  "description": "Implement validation for employee creation"
}
```

**Response:**
```json
{
  "success": true,
  "task": {
    "id": "add-employee-validation",
    "summary": "Add employee validation logic",
    "created": "2025-01-01T16:00:00Z",
    "active": false
  }
}
```

### POST `/tasks/save-context`

Save current editor context for active task.

**Response:**
```json
{
  "success": true,
  "taskId": "work-change-type-diff",
  "filesSaved": 5,
  "context": {
    "openFiles": ["..."],
    "activeFile": "...",
    "cursorPosition": {...}
  }
}
```

---

## Refactoring API

### POST `/refactor/rename`

Rename a symbol (class, method, variable, file).

**Request:**
```json
{
  "filePath": "src/main/kotlin/Service.kt",
  "offset": 150,
  "newName": "EmployeeService"
}
```

**Alternative (line/column):**
```json
{
  "filePath": "src/main/kotlin/Service.kt",
  "line": 10,
  "column": 7,
  "newName": "EmployeeService"
}
```

**Response:**
```json
{
  "success": true,
  "operation": "rename",
  "oldName": "Service",
  "newName": "EmployeeService",
  "filesChanged": [
    "src/main/kotlin/Service.kt",
    "src/main/kotlin/Controller.kt",
    "src/test/kotlin/ServiceTest.kt"
  ],
  "changesCount": 15
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "ELEMENT_NOT_FOUND",
    "message": "No renameable element found at offset 150",
    "details": {
      "filePath": "src/main/kotlin/Service.kt",
      "offset": 150
    }
  }
}
```

### POST `/refactor/extract-method`

Extract selected code into a new method.

**Request:**
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "startOffset": 500,
  "endOffset": 650,
  "methodName": "validateEmployee"
}
```

**Alternative (line-based selection):**
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "startLine": 20,
  "endLine": 25,
  "methodName": "validateEmployee"
}
```

**Response:**
```json
{
  "success": true,
  "operation": "extract-method",
  "methodName": "validateEmployee",
  "methodSignature": "private fun validateEmployee(employee: Employee): Boolean",
  "insertionPoint": {
    "line": 50,
    "column": 5
  },
  "filesChanged": ["src/main/kotlin/EmployeeService.kt"]
}
```

### POST `/refactor/extract-variable`

Extract expression into a variable.

**Request:**
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "startOffset": 300,
  "endOffset": 350,
  "variableName": "isValid"
}
```

**Response:**
```json
{
  "success": true,
  "operation": "extract-variable",
  "variableName": "isValid",
  "variableDeclaration": "val isValid = employee.status == Status.ACTIVE",
  "filesChanged": ["src/main/kotlin/EmployeeService.kt"]
}
```

### POST `/refactor/move-class`

Move class to different package.

**Request:**
```json
{
  "filePath": "src/main/kotlin/Service.kt",
  "targetPackage": "com.hibob.employee.service"
}
```

**Response:**
```json
{
  "success": true,
  "operation": "move-class",
  "className": "EmployeeService",
  "oldPackage": "com.hibob.service",
  "newPackage": "com.hibob.employee.service",
  "oldPath": "src/main/kotlin/Service.kt",
  "newPath": "src/main/kotlin/com/hibob/employee/service/EmployeeService.kt",
  "filesChanged": [
    "src/main/kotlin/com/hibob/employee/service/EmployeeService.kt",
    "src/main/kotlin/Controller.kt",
    "src/test/kotlin/EmployeeServiceTest.kt"
  ]
}
```

### POST `/refactor/inline`

Inline method or variable.

**Request:**
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "offset": 200,
  "inlineAll": false
}
```

**Response:**
```json
{
  "success": true,
  "operation": "inline",
  "elementType": "method",
  "elementName": "validateEmployee",
  "occurrencesInlined": 1,
  "filesChanged": ["src/main/kotlin/EmployeeService.kt"]
}
```

---

## Navigation API

### POST `/navigation/open-file`

Open file in editor, optionally at specific line.

**Request:**
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "line": 42,
  "column": 15
}
```

**Response:**
```json
{
  "success": true,
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "fileOpened": true,
  "cursorPosition": {
    "line": 42,
    "column": 15
  }
}
```

### POST `/navigation/jump-to-definition`

Jump to definition of symbol at cursor.

**Request:**
```json
{
  "filePath": "src/main/kotlin/Controller.kt",
  "offset": 450
}
```

**Response:**
```json
{
  "success": true,
  "definition": {
    "filePath": "src/main/kotlin/EmployeeService.kt",
    "line": 15,
    "column": 7,
    "elementType": "class",
    "elementName": "EmployeeService"
  },
  "navigated": true
}
```

### POST `/navigation/find-usages`

Find all usages of symbol at cursor.

**Request:**
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "offset": 150
}
```

**Response:**
```json
{
  "success": true,
  "element": {
    "type": "method",
    "name": "processEmployee"
  },
  "usages": [
    {
      "filePath": "src/main/kotlin/Controller.kt",
      "line": 25,
      "column": 20,
      "snippet": "service.processEmployee(employee)"
    },
    {
      "filePath": "src/test/kotlin/EmployeeServiceTest.kt",
      "line": 42,
      "column": 15,
      "snippet": "service.processEmployee(testEmployee)"
    }
  ],
  "totalUsages": 2
}
```

### GET `/navigation/project-structure`

Get project structure (packages, files).

**Query Parameters:**
- `depth` (optional): Maximum depth to traverse (default: 3)
- `includeTests` (optional): Include test sources (default: true)

**Response:**
```json
{
  "project": "payroll-hub-data",
  "structure": {
    "src/main/kotlin": {
      "com/hibob/employee": {
        "files": ["Employee.kt", "EmployeeService.kt"],
        "subpackages": ["dto", "repository"]
      }
    },
    "src/test/kotlin": {
      "com/hibob/employee": {
        "files": ["EmployeeServiceTest.kt"]
      }
    }
  }
}
```

---

## Error Codes

| Code | Description |
|------|-------------|
| `TASK_NOT_FOUND` | Task with specified ID does not exist |
| `ELEMENT_NOT_FOUND` | No renameable/refactorable element found at position |
| `FILE_NOT_FOUND` | File does not exist in project |
| `INVALID_REQUEST` | Malformed request body |
| `REFACTORING_FAILED` | Refactoring operation failed |
| `CONTEXT_SAVE_FAILED` | Failed to save task context |
| `NO_ACTIVE_TASK` | No task is currently active |
| `INVALID_PACKAGE` | Target package name is invalid |
| `PERMISSION_DENIED` | Operation not permitted (file read-only, etc.) |

---

## Common Response Format

All endpoints follow this structure:

**Success:**
```json
{
  "success": true,
  "data": { /* endpoint-specific data */ }
}
```

**Error:**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": { /* optional error details */ }
  }
}
```

---

## Rate Limiting

- No rate limiting currently implemented
- Recommend max 100 requests/second for safety
- Future versions may add configurable rate limits

---

## CORS

- CORS is disabled by default (localhost only)
- Can be enabled via config for browser-based clients
- Not recommended for production use

