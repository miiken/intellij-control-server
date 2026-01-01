# Phase 2: Tasks API

**Timeline**: Week 2  
**Status**: Not Started  
**Goal**: Complete task management functionality

## Overview

This phase implements the full Tasks API, allowing external clients to manage IntelliJ tasks and their associated contexts. This is a core feature that enables seamless task switching with automatic context restoration.

## Objectives

1. **Task Listing**: Retrieve all tasks with details
2. **Current Task**: Get active task information
3. **Task Switching**: Switch tasks with context restoration
4. **Task Creation**: Create new tasks programmatically
5. **Context Management**: Save and restore editor states

## Deliverables

- [ ] `GET /tasks/list` - List all tasks
- [ ] `GET /tasks/current` - Get current active task
- [ ] `POST /tasks/switch` - Switch to different task
- [ ] `POST /tasks/create` - Create new task
- [ ] `POST /tasks/save-context` - Save current context
- [ ] Full context save/restore logic
- [ ] Error handling for task operations
- [ ] Unit tests for TaskManagerService
- [ ] Integration tests

## Success Criteria

```bash
# List tasks
curl http://localhost:8765/tasks/list
# Returns: {"tasks": [...]}

# Create task
curl -X POST http://localhost:8765/tasks/create \
  -H "Content-Type: application/json" \
  -d '{"summary": "Test task", "description": "Testing"}'
# Returns: {"success": true, "task": {...}}

# Switch task (restores context)
curl -X POST http://localhost:8765/tasks/switch \
  -H "Content-Type: application/json" \
  -d '{"taskId": "test-task"}'
# Returns: {"success": true, "contextRestored": true}
```

## Key Files to Create

- `src/main/kotlin/io/hibob/intellijcontrolserver/services/TaskManagerService.kt`
- `src/main/kotlin/io/hibob/intellijcontrolserver/server/handlers/TasksHandler.kt`
- `src/main/kotlin/io/hibob/intellijcontrolserver/model/TaskInfo.kt`
- `src/test/kotlin/io/hibob/intellijcontrolserver/services/TaskManagerServiceTest.kt`

## IntelliJ APIs Used

- `TaskManager` - Task management
- `ContextManager` - Context save/restore
- `FileEditorManager` - Open files management
- `PsiManager` - Code structure access

## Dependencies

This phase depends on:
- ✅ Phase 1: HTTP server infrastructure

## Next Phase

→ **Phase 3**: Basic Refactoring (Rename)

