# Phase 3: Basic Refactoring

**Timeline**: Week 3-4  
**Status**: Not Started  
**Goal**: Rename refactoring (most common operation)

## Overview

This phase implements the rename refactoring operation, which is the most commonly used refactoring in IntelliJ. Rename works across multiple files and maintains type safety, making it a critical feature for AI-assisted development.

## Objectives

1. **PSI Element Resolution**: Convert file offset to PSI element
2. **Rename Processor**: Integrate with IntelliJ's rename refactoring
3. **Multi-file Support**: Handle renames across multiple files
4. **Conflict Detection**: Detect and report naming conflicts
5. **Preview Support**: Optional preview before applying

## Deliverables

- [ ] `POST /refactor/rename` endpoint
- [ ] PSI element resolution from offset/line/column
- [ ] Rename processor integration
- [ ] Multi-file refactoring support
- [ ] Conflict detection and reporting
- [ ] Error handling for refactoring failures
- [ ] Unit tests for RefactoringService
- [ ] Integration tests with sample projects

## Success Criteria

```bash
# Rename a class
curl -X POST http://localhost:8765/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/Service.kt",
    "offset": 150,
    "newName": "EmployeeService"
  }'

# Expected response:
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

## Key Files to Create

- `src/main/kotlin/io/hibob/intellijcontrolserver/services/RefactoringService.kt`
- `src/main/kotlin/io/hibob/intellijcontrolserver/server/handlers/RefactoringHandler.kt`
- `src/main/kotlin/io/hibob/intellijcontrolserver/model/RefactoringRequest.kt`
- `src/main/kotlin/io/hibob/intellijcontrolserver/util/PsiUtils.kt`
- `src/test/kotlin/io/hibob/intellijcontrolserver/services/RefactoringServiceTest.kt`

## IntelliJ APIs Used

- `PsiManager` - Access PSI (Program Structure Interface)
- `PsiFile` - File representation
- `PsiElement` - Code element (class, method, variable)
- `RefactoringFactory` - Create refactoring processors
- `RenameProcessor` - Perform rename operation
- `ConflictsDialog` - Detect conflicts

## Dependencies

This phase depends on:
- ✅ Phase 1: HTTP server infrastructure
- ⚠️ Phase 2: Not strictly required, but useful for testing

## Next Phase

→ **Phase 4**: Advanced Refactoring (Extract, Move, Inline)

