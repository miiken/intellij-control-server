# Phase 5: Navigation API

**Timeline**: Week 7  
**Status**: Not Started  
**Goal**: File navigation and code browsing

## Overview

This phase implements navigation and code exploration APIs that enable external tools to control IntelliJ's navigation features. These operations are essential for AI tools that need to explore the codebase, jump between definitions, and understand code structure.

## Objectives

1. **Open Files**: Open files at specific lines/columns
2. **Jump to Definition**: Navigate to symbol definitions
3. **Find Usages**: Find all references to a symbol
4. **Project Structure**: Query project packages and files

## Deliverables

- [ ] `POST /navigation/open-file` endpoint
- [ ] `POST /navigation/jump-to-definition` endpoint
- [ ] `POST /navigation/find-usages` endpoint
- [ ] `GET /navigation/project-structure` endpoint
- [ ] Cursor position handling
- [ ] Usage context and snippets
- [ ] Unit and integration tests

## Success Criteria

### Open File
```bash
curl -X POST http://localhost:8765/navigation/open-file \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/EmployeeService.kt",
    "line": 42,
    "column": 15
  }'

# Response:
{
  "success": true,
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "fileOpened": true,
  "cursorPosition": {"line": 42, "column": 15}
}
```

### Jump to Definition
```bash
curl -X POST http://localhost:8765/navigation/jump-to-definition \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/Controller.kt",
    "offset": 450
  }'

# Response:
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

### Find Usages
```bash
curl -X POST http://localhost:8765/navigation/find-usages \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/EmployeeService.kt",
    "offset": 150
  }'

# Response:
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
    }
  ],
  "totalUsages": 2
}
```

### Project Structure
```bash
curl http://localhost:8765/navigation/project-structure?depth=3

# Response:
{
  "project": "payroll-hub-data",
  "structure": {
    "src/main/kotlin": {
      "com/miiken/employee": {
        "files": ["Employee.kt", "EmployeeService.kt"],
        "subpackages": ["dto", "repository"]
      }
    }
  }
}
```

## Key Files to Create

- `src/main/kotlin/io/miiken/intellijcontrolserver/services/NavigationService.kt`
- `src/main/kotlin/io/miiken/intellijcontrolserver/server/handlers/NavigationHandler.kt`
- `src/main/kotlin/io/miiken/intellijcontrolserver/model/NavigationRequest.kt`
- `src/main/kotlin/io/miiken/intellijcontrolserver/model/UsageInfo.kt`

## IntelliJ APIs Used

- `FileEditorManager` - Open/focus files
- `PsiManager` - Access code structure
- `GotoDeclarationAction` - Jump to definition
- `FindUsagesManager` - Find usages
- `PsiDirectory` - Directory structure
- `EditorFactory` - Manage editors
- `LogicalPosition` / `Document` - Cursor positioning

## Dependencies

This phase depends on:
- ✅ Phase 1: HTTP server infrastructure
- ✅ Phase 3: PSI utilities (offset/line conversion)

## Next Phase

→ **Phase 6**: Polish & Documentation

