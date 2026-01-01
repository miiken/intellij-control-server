# Phase 4 Tasks

## 1. Data Models

### 1.1 Extract Method Request
- [ ] Create `ExtractMethodRequest` data class
- [ ] Fields: filePath, startOffset/startLine, endOffset/endLine, methodName
- [ ] Support both offset and line-based selection
- [ ] Validation for required fields

```kotlin
data class ExtractMethodRequest(
    val filePath: String,
    val startOffset: Int? = null,
    val endOffset: Int? = null,
    val startLine: Int? = null,
    val endLine: Int? = null,
    val methodName: String
)
```

### 1.2 Extract Variable Request
- [ ] Create `ExtractVariableRequest` data class
- [ ] Fields: filePath, startOffset, endOffset, variableName
- [ ] Support replaceAll option

```kotlin
data class ExtractVariableRequest(
    val filePath: String,
    val startOffset: Int,
    val endOffset: Int,
    val variableName: String,
    val replaceAll: Boolean = true
)
```

### 1.3 Move Class Request
- [ ] Create `MoveClassRequest` data class
- [ ] Fields: filePath, targetPackage
- [ ] Optional: targetDirectory

```kotlin
data class MoveClassRequest(
    val filePath: String,
    val targetPackage: String
)
```

### 1.4 Inline Request
- [ ] Create `InlineRequest` data class
- [ ] Fields: filePath, offset, inlineAll
- [ ] Support both method and variable inlining

```kotlin
data class InlineRequest(
    val filePath: String,
    val offset: Int,
    val inlineAll: Boolean = false
)
```

### 1.5 Response Models
- [ ] Extend `RefactoringResult` for each operation
- [ ] Include operation-specific details
- [ ] Method signature for extract-method
- [ ] Old/new paths for move-class

## 2. Service Layer Extensions

### 2.1 Extract Method
- [ ] Extend `RefactoringService` with `extractMethod()`
- [ ] Find PSI elements in selection range
- [ ] Validate selection is valid for extraction
- [ ] Create `ExtractMethodProcessor`
- [ ] Execute refactoring
- [ ] Return method signature and location

**Implementation**:
```kotlin
fun extractMethod(
    filePath: String,
    startOffset: Int,
    endOffset: Int,
    methodName: String
): RefactoringResult {
    return ReadAction.compute<RefactoringResult, Throwable> {
        val psiFile = PsiUtils.findPsiFile(project, filePath)
        val startElement = psiFile.findElementAt(startOffset)
        val endElement = psiFile.findElementAt(endOffset)
        
        // Validate selection
        val commonParent = PsiTreeUtil.findCommonParent(startElement, endElement)
        
        WriteCommandAction.runWriteCommandAction(project) {
            val processor = ExtractMethodProcessor(
                project, 
                startElement, 
                endElement, 
                methodName
            )
            processor.run()
        }
        
        RefactoringResult(success = true, ...)
    }
}
```

**Acceptance Criteria**:
- Extracts valid code selections
- Detects return values automatically
- Detects parameters automatically
- Handles local variables correctly
- Places method in appropriate location

### 2.2 Extract Variable
- [ ] Extend `RefactoringService` with `extractVariable()`
- [ ] Find expression in selection
- [ ] Validate expression is extractable
- [ ] Create `IntroduceVariableRefactoring`
- [ ] Execute refactoring
- [ ] Return variable declaration

**Acceptance Criteria**:
- Extracts valid expressions
- Infers correct type
- Replaces all occurrences (if requested)
- Places declaration in correct scope

### 2.3 Move Class
- [ ] Extend `RefactoringService` with `moveClass()`
- [ ] Find class in file
- [ ] Validate target package exists or can be created
- [ ] Create `MoveClassProcessor`
- [ ] Execute refactoring
- [ ] Update imports in all files
- [ ] Return old and new paths

**Acceptance Criteria**:
- Moves class to target package
- Updates package declaration
- Updates imports in all referencing files
- Renames/moves file correctly

### 2.4 Inline
- [ ] Extend `RefactoringService` with `inline()`
- [ ] Find element at offset (method or variable)
- [ ] Determine if method or variable
- [ ] Create appropriate inline processor
- [ ] Execute refactoring
- [ ] Return inline summary

**Acceptance Criteria**:
- Inlines methods correctly
- Inlines variables correctly
- Handles single usage vs all usages
- Removes inlined element when appropriate

## 3. Selection Handling Utilities

### 3.1 Range Utilities
- [ ] Add to `PsiUtils`: `findElementsInRange()`
- [ ] Add to `PsiUtils`: `validateSelection()`
- [ ] Add to `PsiUtils`: `lineRangeToOffsets()`

```kotlin
object PsiUtils {
    fun findElementsInRange(
        psiFile: PsiFile, 
        startOffset: Int, 
        endOffset: Int
    ): List<PsiElement>
    
    fun validateSelection(
        startElement: PsiElement, 
        endElement: PsiElement
    ): Boolean
    
    fun lineRangeToOffsets(
        psiFile: PsiFile, 
        startLine: Int, 
        endLine: Int
    ): Pair<Int, Int>
}
```

### 3.2 Expression Detection
- [ ] Detect if selection is valid expression
- [ ] Find smallest expression containing selection
- [ ] Validate expression has meaningful type

## 4. HTTP Handler Extensions

### 4.1 POST /refactor/extract-method
- [ ] Parse `ExtractMethodRequest`
- [ ] Validate selection (start < end)
- [ ] Validate method name is valid identifier
- [ ] Call `refactoringService.extractMethod()`
- [ ] Return result

**Request**:
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "startOffset": 500,
  "endOffset": 650,
  "methodName": "validateEmployee"
}
```

**Response**:
```json
{
  "success": true,
  "operation": "extract-method",
  "methodName": "validateEmployee",
  "methodSignature": "private fun validateEmployee(employee: Employee): Boolean",
  "insertionPoint": {"line": 50, "column": 5},
  "filesChanged": ["src/main/kotlin/EmployeeService.kt"]
}
```

### 4.2 POST /refactor/extract-variable
- [ ] Parse `ExtractVariableRequest`
- [ ] Validate selection
- [ ] Validate variable name
- [ ] Call `refactoringService.extractVariable()`
- [ ] Return result

**Request**:
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "startOffset": 300,
  "endOffset": 350,
  "variableName": "isValid"
}
```

**Response**:
```json
{
  "success": true,
  "operation": "extract-variable",
  "variableName": "isValid",
  "variableDeclaration": "val isValid = employee.status == Status.ACTIVE",
  "filesChanged": ["src/main/kotlin/EmployeeService.kt"]
}
```

### 4.3 POST /refactor/move-class
- [ ] Parse `MoveClassRequest`
- [ ] Validate package name format
- [ ] Call `refactoringService.moveClass()`
- [ ] Return result with path changes

**Request**:
```json
{
  "filePath": "src/main/kotlin/Service.kt",
  "targetPackage": "com.miiken.employee.service"
}
```

**Response**:
```json
{
  "success": true,
  "operation": "move-class",
  "className": "EmployeeService",
  "oldPackage": "com.miiken.service",
  "newPackage": "com.miiken.employee.service",
  "oldPath": "src/main/kotlin/Service.kt",
  "newPath": "src/main/kotlin/com/miiken/employee/service/EmployeeService.kt",
  "filesChanged": [
    "src/main/kotlin/com/miiken/employee/service/EmployeeService.kt",
    "src/main/kotlin/Controller.kt",
    "src/test/kotlin/EmployeeServiceTest.kt"
  ]
}
```

### 4.4 POST /refactor/inline
- [ ] Parse `InlineRequest`
- [ ] Find element at offset
- [ ] Call `refactoringService.inline()`
- [ ] Return result

**Request**:
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "offset": 200,
  "inlineAll": false
}
```

**Response**:
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

## 5. Error Handling

### 5.1 Invalid Selection
- [ ] Detect invalid code selections
- [ ] Return `INVALID_SELECTION` error
- [ ] Explain what makes selection invalid

### 5.2 Cannot Extract
- [ ] Detect when code cannot be extracted
- [ ] Return `CANNOT_EXTRACT` error
- [ ] Explain reason (e.g., multiple return points)

### 5.3 Invalid Package
- [ ] Validate package name format
- [ ] Return `INVALID_PACKAGE` error

### 5.4 Package Conflict
- [ ] Detect class name conflicts in target package
- [ ] Return `PACKAGE_CONFLICT` error

### 5.5 Cannot Inline
- [ ] Detect when element cannot be inlined
- [ ] Return `CANNOT_INLINE` error
- [ ] Explain reason (e.g., recursive method)

## 6. Testing

### 6.1 Extract Method Tests
- [ ] Test simple code block extraction
- [ ] Test extraction with return value
- [ ] Test extraction with parameters
- [ ] Test extraction with local variables
- [ ] Test invalid selections
- [ ] Test method placement

### 6.2 Extract Variable Tests
- [ ] Test expression extraction
- [ ] Test replace all occurrences
- [ ] Test replace single occurrence
- [ ] Test type inference
- [ ] Test scope placement

### 6.3 Move Class Tests
- [ ] Test simple class move
- [ ] Test move with imports update
- [ ] Test move to new package
- [ ] Test move with references in other files
- [ ] Test file rename/move

### 6.4 Inline Tests
- [ ] Test inline method (single usage)
- [ ] Test inline method (all usages)
- [ ] Test inline variable
- [ ] Test remove inlined element

### 6.5 Integration Tests
- [ ] Create complex test projects
- [ ] Test end-to-end workflows
- [ ] Test multiple refactorings in sequence
- [ ] Verify IntelliJ sees all changes

**File**: `src/test/kotlin/io/miiken/intellijcontrolserver/integration/AdvancedRefactoringTest.kt`

### 6.6 Manual Testing
- [ ] Create sample project with complex code
- [ ] Test each refactoring via API
- [ ] Verify results in IntelliJ
- [ ] Test error cases
- [ ] Test undo/redo behavior

## 7. Edge Cases

### 7.1 Extract Method Edge Cases
- [ ] Multiple return points
- [ ] Extracted code modifies outer variables
- [ ] Extracted code uses outer class members
- [ ] Extracted code throws exceptions

### 7.2 Extract Variable Edge Cases
- [ ] Expression with side effects
- [ ] Expression used in different scopes
- [ ] Expression type is complex generic

### 7.3 Move Class Edge Cases
- [ ] Inner classes
- [ ] Classes with companions objects
- [ ] Classes with annotations
- [ ] Sealed classes with subclasses

### 7.4 Inline Edge Cases
- [ ] Recursive methods
- [ ] Methods with multiple return points
- [ ] Variables with multiple assignments

## 8. Documentation

- [ ] Document all endpoints in API-SPEC.md
- [ ] Add usage examples for each operation
- [ ] Document error codes
- [ ] Add troubleshooting guide
- [ ] Update README with examples

## 9. Performance Optimization

- [ ] Optimize PSI tree traversal
- [ ] Cache PSI file lookups
- [ ] Batch import updates
- [ ] Profile refactoring operations

## Completion Checklist

- [ ] All tasks above completed
- [ ] All 4 endpoints working
- [ ] Extract method working
- [ ] Extract variable working
- [ ] Move class working
- [ ] Inline working
- [ ] All error cases handled
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] Manual testing successful
- [ ] Documentation updated
- [ ] Ready for Phase 5

## Estimated Time

- Data models: 3 hours
- Extract method: 10 hours
- Extract variable: 6 hours
- Move class: 8 hours
- Inline: 6 hours
- Selection utilities: 4 hours
- HTTP handlers: 4 hours
- Error handling: 4 hours
- Testing: 12 hours
- Documentation: 3 hours

**Total**: ~60 hours (7.5 days)

