# Phase 2: Basic Refactoring

## Overview
Implement core refactoring operations that are most commonly used: rename (classes, methods, variables) and extract method. These operations form the foundation for AI-assisted code refactoring.

## Goals
- ✅ Implement rename refactoring for all symbol types
- ✅ Implement extract method refactoring
- ✅ Handle multi-file refactoring scenarios
- ✅ Provide conflict detection and resolution
- ✅ Ensure thread-safe PSI operations

## Deliverables

### API Endpoints
1. **POST `/refactor/rename`** - Rename classes, methods, variables, parameters
2. **POST `/refactor/extract-method`** - Extract selected code into a new method

### Core Components
- `RefactoringService` - Business logic for refactoring operations
- `RefactoringHandler` - HTTP request handler
- PSI utilities for element resolution
- Thread-safe refactoring execution

## Success Criteria
- [ ] Can rename classes across multiple files
- [ ] Can rename methods with all usages updated
- [ ] Can rename variables within scope
- [ ] Can extract method from selection
- [ ] Handles conflicts gracefully
- [ ] All operations work in WriteCommandAction
- [ ] Unit tests for all refactoring operations
- [ ] Integration tests with real IntelliJ projects

## Key Files to Create
```
src/main/kotlin/io/miiken/intellijcontrolserver/
├── services/
│   └── RefactoringService.kt
├── server/handlers/
│   └── RefactoringHandler.kt
└── util/
    └── PsiUtils.kt

src/test/kotlin/io/miiken/intellijcontrolserver/
└── services/
    └── RefactoringServiceTest.kt
```

## IntelliJ APIs to Use
- `PsiManager` - PSI file access
- `PsiElement` - Code elements
- `RenameProcessor` - Rename refactoring
- `ExtractMethodProcessor` - Extract method refactoring
- `WriteCommandAction` - Safe PSI modifications
- `ReadAction` - Safe PSI reading

## Dependencies
- Phase 1 (Foundation) must be complete
- HTTP server infrastructure
- Configuration system
- Response utilities

## Estimated Effort
**Time**: 2-3 days
**Complexity**: Medium-High (IntelliJ PSI and refactoring APIs)

## Testing Strategy
1. Unit tests for PSI element resolution
2. Unit tests for refactoring logic
3. Integration tests with sample Kotlin/Java projects
4. Manual testing in sandbox IDE
5. Test with various code scenarios (classes, methods, variables)

## Notes
- Refactoring must run on EDT (Event Dispatch Thread)
- Use `WriteCommandAction` for all PSI modifications
- Handle conflicts (e.g., name already exists)
- Support preview mode (optional for v1.0)
- Focus on Kotlin first, Java second

