# Phase 2 Tasks

## 1. Data Models

### 1.1 TaskInfo Model
- [ ] Create `TaskInfo` data class
- [ ] Fields: id, summary, description, created, updated, active, project
- [ ] Add context information (openFiles, activeFile, cursorPosition)
- [ ] JSON serialization support

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/model/TaskInfo.kt`

### 1.2 Request/Response Models
- [ ] `CreateTaskRequest` - summary, description
- [ ] `SwitchTaskRequest` - taskId
- [ ] `TaskListResponse` - tasks array
- [ ] `TaskSwitchResponse` - success, previousTask, currentTask, contextRestored

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/model/Tasks.kt`

## 2. Service Layer

### 2.1 TaskManagerService Implementation
- [ ] Create `TaskManagerService` class
- [ ] Get reference to `TaskManager.getInstance(project)`
- [ ] Get reference to `ContextManager.getInstance(project)`
- [ ] Implement `getAllTasks()` method
- [ ] Implement `getCurrentTask()` method
- [ ] Implement `switchTask(taskId)` method
- [ ] Implement `createTask(summary, description)` method

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/services/TaskManagerService.kt`

**Methods**:
```kotlin
class TaskManagerService(private val project: Project) {
    fun getAllTasks(): List<TaskInfo>
    fun getCurrentTask(): TaskInfo?
    fun switchTask(taskId: String): SwitchResult
    fun createTask(summary: String, description: String?): TaskInfo
    fun saveContext(): Boolean
}
```

### 2.2 Context Management
- [ ] Extract open files from `FileEditorManager`
- [ ] Get active editor and cursor position
- [ ] Save context before task switch
- [ ] Restore context after task switch
- [ ] Handle missing files gracefully

**Acceptance Criteria**:
- Context saved before switching
- All previously open files restored
- Active file and cursor position restored
- Graceful handling of moved/deleted files

## 3. HTTP Handlers

### 3.1 TasksHandler Base
- [ ] Create `TasksHandler` implementing `HttpHandler`
- [ ] Route to appropriate method based on path
- [ ] Parse JSON request bodies
- [ ] Format JSON responses
- [ ] Handle errors with standard error format

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/server/handlers/TasksHandler.kt`

### 3.2 GET /tasks/list
- [ ] Handle GET request
- [ ] Call `taskManagerService.getAllTasks()`
- [ ] Convert to JSON response
- [ ] Handle empty task list

**Response**:
```json
{
  "tasks": [
    {
      "id": "task-id",
      "summary": "Task summary",
      "active": true
    }
  ]
}
```

### 3.3 GET /tasks/current
- [ ] Handle GET request
- [ ] Call `taskManagerService.getCurrentTask()`
- [ ] Include context information
- [ ] Return null if no active task

**Response**:
```json
{
  "task": {
    "id": "task-id",
    "summary": "Current task",
    "context": {
      "openFiles": ["file1.kt", "file2.kt"],
      "activeFile": "file1.kt",
      "cursorPosition": {"line": 42, "column": 15}
    }
  }
}
```

### 3.4 POST /tasks/switch
- [ ] Handle POST request
- [ ] Parse `SwitchTaskRequest` from body
- [ ] Validate taskId exists
- [ ] Call `taskManagerService.switchTask(taskId)`
- [ ] Return switch result

**Request**:
```json
{
  "taskId": "target-task-id"
}
```

**Response**:
```json
{
  "success": true,
  "previousTask": "old-task-id",
  "currentTask": "target-task-id",
  "contextRestored": true,
  "filesOpened": ["file1.kt", "file2.kt"]
}
```

**Error Response**:
```json
{
  "success": false,
  "error": {
    "code": "TASK_NOT_FOUND",
    "message": "Task with ID 'invalid-id' not found"
  }
}
```

### 3.5 POST /tasks/create
- [ ] Handle POST request
- [ ] Parse `CreateTaskRequest` from body
- [ ] Validate summary is not empty
- [ ] Call `taskManagerService.createTask()`
- [ ] Return created task info

**Request**:
```json
{
  "summary": "New task",
  "description": "Optional description"
}
```

**Response**:
```json
{
  "success": true,
  "task": {
    "id": "new-task-id",
    "summary": "New task",
    "created": "2026-01-01T10:00:00Z",
    "active": false
  }
}
```

### 3.6 POST /tasks/save-context
- [ ] Handle POST request
- [ ] Get current active task
- [ ] Call `taskManagerService.saveContext()`
- [ ] Return saved context summary

**Response**:
```json
{
  "success": true,
  "taskId": "current-task-id",
  "filesSaved": 5,
  "context": {
    "openFiles": ["..."],
    "activeFile": "...",
    "cursorPosition": {...}
  }
}
```

## 4. Threading & Safety

### 4.1 Read Operations
- [ ] Wrap task list retrieval in `ReadAction.compute`
- [ ] Wrap file editor queries in `ReadAction`
- [ ] Handle read access exceptions

### 4.2 Write Operations
- [ ] Wrap task switching in `WriteCommandAction`
- [ ] Wrap context restoration in `WriteCommandAction`
- [ ] Handle write access exceptions

**Example**:
```kotlin
fun switchTask(taskId: String): SwitchResult {
    return ReadAction.compute<SwitchResult, Throwable> {
        val task = findTask(taskId)
        WriteCommandAction.runWriteCommandAction(project) {
            taskManager.activateTask(task)
            contextManager.restoreContext(task)
        }
        SwitchResult(success = true)
    }
}
```

## 5. Error Handling

### 5.1 Task Not Found
- [ ] Detect when taskId doesn't exist
- [ ] Return 404 with `TASK_NOT_FOUND` error code
- [ ] Include attempted taskId in details

### 5.2 No Active Task
- [ ] Handle case when no task is active
- [ ] Return `NO_ACTIVE_TASK` error code
- [ ] Suggest creating or switching to a task

### 5.3 Context Save Failed
- [ ] Catch context save errors
- [ ] Return `CONTEXT_SAVE_FAILED` error code
- [ ] Log full error details

### 5.4 Invalid Request
- [ ] Validate JSON structure
- [ ] Validate required fields
- [ ] Return `INVALID_REQUEST` with details

## 6. Testing

### 6.1 Unit Tests
- [ ] Test `TaskManagerService.getAllTasks()`
- [ ] Test `TaskManagerService.switchTask()` with valid task
- [ ] Test `TaskManagerService.switchTask()` with invalid task
- [ ] Test context save/restore logic
- [ ] Test `TasksHandler` routing
- [ ] Test JSON parsing/formatting

**File**: `src/test/kotlin/io/hibob/intellijcontrolserver/services/TaskManagerServiceTest.kt`

### 6.2 Integration Tests
- [ ] Create test project with tasks
- [ ] Test task listing via API
- [ ] Test task creation via API
- [ ] Test task switching with context restore
- [ ] Verify files actually open in editor
- [ ] Verify cursor position restored

**File**: `src/test/kotlin/io/hibob/intellijcontrolserver/integration/TasksApiTest.kt`

### 6.3 Manual Testing
- [ ] Start sandbox IntelliJ: `./gradlew runIde`
- [ ] Create tasks manually in IntelliJ
- [ ] Test listing: `curl http://localhost:8765/tasks/list`
- [ ] Test creation via API
- [ ] Test switching, verify files open
- [ ] Test save-context endpoint

## 7. Documentation

- [ ] Document all endpoints in API-SPEC.md
- [ ] Add usage examples to README
- [ ] Document error codes
- [ ] Add troubleshooting section

## Completion Checklist

- [ ] All tasks above completed
- [ ] All 5 endpoints working
- [ ] Context save/restore working
- [ ] Error handling comprehensive
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] Manual testing successful
- [ ] Documentation updated
- [ ] Ready for Phase 3

## Estimated Time

- Data models: 2 hours
- Service layer: 6 hours
- HTTP handlers: 6 hours
- Threading safety: 3 hours
- Error handling: 2 hours
- Testing: 6 hours
- Documentation: 2 hours

**Total**: ~27 hours (3.5 days)

