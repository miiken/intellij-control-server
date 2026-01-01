# Phase 4: Advanced Refactoring

**Timeline**: Week 5-6  
**Status**: Not Started  
**Goal**: Extract method, extract variable, move class, inline

## Overview

This phase implements advanced refactoring operations that complement the basic rename functionality. These operations enable more sophisticated code transformations and are essential for AI-assisted refactoring workflows.

## Objectives

1. **Extract Method**: Extract code selection into a new method
2. **Extract Variable**: Extract expression into a variable
3. **Move Class**: Move class to different package
4. **Inline**: Inline method or variable into usage sites

## Deliverables

- [ ] `POST /refactor/extract-method` endpoint
- [ ] `POST /refactor/extract-variable` endpoint
- [ ] `POST /refactor/move-class` endpoint
- [ ] `POST /refactor/inline` endpoint
- [ ] Selection handling (start/end offsets)
- [ ] Package validation for move operations
- [ ] Import management
- [ ] Unit and integration tests

## Success Criteria

### Extract Method
```bash
curl -X POST http://localhost:8765/refactor/extract-method \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/EmployeeService.kt",
    "startOffset": 500,
    "endOffset": 650,
    "methodName": "validateEmployee"
  }'

# Response:
{
  "success": true,
  "operation": "extract-method",
  "methodName": "validateEmployee",
  "methodSignature": "private fun validateEmployee(employee: Employee): Boolean",
  "filesChanged": ["src/main/kotlin/EmployeeService.kt"]
}
```

### Extract Variable
```bash
curl -X POST http://localhost:8765/refactor/extract-variable \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/EmployeeService.kt",
    "startOffset": 300,
    "endOffset": 350,
    "variableName": "isValid"
  }'
```

### Move Class
```bash
curl -X POST http://localhost:8765/refactor/move-class \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/Service.kt",
    "targetPackage": "com.miiken.employee.service"
  }'
```

### Inline
```bash
curl -X POST http://localhost:8765/refactor/inline \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/EmployeeService.kt",
    "offset": 200,
    "inlineAll": false
  }'
```

## Key Files to Create

- `src/main/kotlin/io/miiken/intellijcontrolserver/services/RefactoringService.kt` (extend)
- `src/main/kotlin/io/miiken/intellijcontrolserver/server/handlers/RefactoringHandler.kt` (extend)
- `src/main/kotlin/io/miiken/intellijcontrolserver/model/RefactoringRequest.kt` (extend)

## IntelliJ APIs Used

- `ExtractMethodProcessor` - Extract method refactoring
- `IntroduceVariableRefactoring` - Extract variable
- `MoveClassProcessor` - Move class to package
- `InlineMethodProcessor` / `InlineVariableProcessor` - Inline operations
- `PsiPackage` - Package management
- `ImportOptimizer` - Clean up imports

## Dependencies

This phase depends on:
- ✅ Phase 1: HTTP server infrastructure
- ✅ Phase 3: Basic refactoring (PSI utilities, error handling patterns)

## Next Phase

→ **Phase 5**: Navigation API

