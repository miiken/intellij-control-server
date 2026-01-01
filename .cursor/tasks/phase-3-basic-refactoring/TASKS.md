# Phase 3 Tasks

## 1. Data Models

### 1.1 Refactoring Request Models
- [ ] Create `RenameRequest` data class
- [ ] Support offset-based position: filePath, offset, newName
- [ ] Support line/column position: filePath, line, column, newName
- [ ] Validation for required fields

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/model/RefactoringRequest.kt`

```kotlin
data class RenameRequest(
    val filePath: String,
    val offset: Int? = null,
    val line: Int? = null,
    val column: Int? = null,
    val newName: String
)
```

### 1.2 Refactoring Response Models
- [ ] Create `RenameResult` data class
- [ ] Fields: success, operation, oldName, newName, filesChanged, changesCount
- [ ] Include error details on failure

```kotlin
data class RenameResult(
    val success: Boolean,
    val operation: String = "rename",
    val oldName: String? = null,
    val newName: String? = null,
    val filesChanged: List<String> = emptyList(),
    val changesCount: Int = 0,
    val error: ErrorInfo? = null
)
```

## 2. PSI Utilities

### 2.1 Element Resolution
- [ ] Create `PsiUtils` object
- [ ] Implement `findPsiFile(project, filePath)`
- [ ] Implement `offsetToElement(psiFile, offset)`
- [ ] Implement `lineColumnToOffset(psiFile, line, column)`
- [ ] Implement `lineColumnToElement(psiFile, line, column)`

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/util/PsiUtils.kt`

```kotlin
object PsiUtils {
    fun findPsiFile(project: Project, filePath: String): PsiFile?
    fun offsetToElement(psiFile: PsiFile, offset: Int): PsiElement?
    fun lineColumnToOffset(psiFile: PsiFile, line: Int, column: Int): Int
    fun findRenameableElement(element: PsiElement): PsiNamedElement?
}
```

**Acceptance Criteria**:
- Finds correct PSI element at offset
- Handles invalid offsets gracefully
- Converts line/column to offset correctly
- Finds parent renameable element (e.g., class name from inside class)

### 2.2 Element Validation
- [ ] Check if element is renameable (`PsiNamedElement`)
- [ ] Check if element is read-only
- [ ] Check if file is in project scope
- [ ] Validate new name is legal identifier

## 3. Service Layer

### 3.1 RefactoringService Implementation
- [ ] Create `RefactoringService` class
- [ ] Constructor takes `Project` parameter
- [ ] Implement `rename()` method
- [ ] Handle both offset and line/column input
- [ ] Track affected files

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/services/RefactoringService.kt`

```kotlin
class RefactoringService(private val project: Project) {
    fun rename(
        filePath: String,
        offset: Int?,
        line: Int?,
        column: Int?,
        newName: String
    ): RenameResult {
        // Implementation
    }
}
```

### 3.2 Rename Logic
- [ ] Find PSI file from path
- [ ] Resolve PSI element from offset or line/column
- [ ] Find renameable element (navigate to parent if needed)
- [ ] Create `RenameProcessor`
- [ ] Check for conflicts
- [ ] Execute rename refactoring
- [ ] Collect affected files

**Flow**:
```kotlin
// 1. Find element
val psiFile = PsiUtils.findPsiFile(project, filePath)
val element = if (offset != null) {
    PsiUtils.offsetToElement(psiFile, offset)
} else {
    PsiUtils.lineColumnToElement(psiFile, line!!, column!!)
}

// 2. Create refactoring
val processor = RenameProcessor(project, element, newName, false, false)

// 3. Check conflicts
if (processor.findConflicts() != null) {
    // Handle conflicts
}

// 4. Execute
WriteCommandAction.runWriteCommandAction(project) {
    processor.run()
}
```

### 3.3 Threading Safety
- [ ] Wrap PSI reads in `ReadAction.compute`
- [ ] Wrap refactoring execution in `WriteCommandAction`
- [ ] Handle thread exceptions

**Example**:
```kotlin
fun rename(...): RenameResult {
    return ReadAction.compute<RenameResult, Throwable> {
        val element = findElement(...)
        val oldName = element.name
        
        WriteCommandAction.runWriteCommandAction(project) {
            val processor = RenameProcessor(...)
            processor.run()
        }
        
        RenameResult(
            success = true,
            oldName = oldName,
            newName = newName
        )
    }
}
```

## 4. HTTP Handler

### 4.1 RefactoringHandler Base
- [ ] Create `RefactoringHandler` implementing `HttpHandler`
- [ ] Route based on path: `/refactor/rename`, `/refactor/extract-method`, etc.
- [ ] Parse JSON request bodies
- [ ] Format JSON responses
- [ ] Standard error handling

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/server/handlers/RefactoringHandler.kt`

### 4.2 POST /refactor/rename
- [ ] Handle POST request
- [ ] Parse `RenameRequest` from body
- [ ] Validate required fields (filePath, newName, offset OR line+column)
- [ ] Call `refactoringService.rename()`
- [ ] Return result as JSON

**Request**:
```json
{
  "filePath": "src/main/kotlin/Service.kt",
  "offset": 150,
  "newName": "EmployeeService"
}
```

**OR**:
```json
{
  "filePath": "src/main/kotlin/Service.kt",
  "line": 10,
  "column": 7,
  "newName": "EmployeeService"
}
```

**Success Response**:
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

**Error Response**:
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

## 5. Error Handling

### 5.1 File Not Found
- [ ] Detect when file doesn't exist
- [ ] Return `FILE_NOT_FOUND` error code
- [ ] Include attempted file path

### 5.2 Element Not Found
- [ ] Detect when no element at offset
- [ ] Return `ELEMENT_NOT_FOUND` error code
- [ ] Include position details

### 5.3 Not Renameable
- [ ] Detect when element can't be renamed
- [ ] Return `NOT_RENAMEABLE` error code
- [ ] Explain what element was found

### 5.4 Naming Conflicts
- [ ] Detect naming conflicts
- [ ] Return `NAMING_CONFLICT` error code
- [ ] Include conflict details

### 5.5 Invalid Name
- [ ] Validate new name is legal identifier
- [ ] Return `INVALID_NAME` error code
- [ ] Explain naming rules

### 5.6 Read-Only File
- [ ] Detect read-only files
- [ ] Return `PERMISSION_DENIED` error code

### 5.7 Refactoring Failed
- [ ] Catch general refactoring exceptions
- [ ] Return `REFACTORING_FAILED` error code
- [ ] Log full error details

## 6. Testing

### 6.1 PSI Utils Tests
- [ ] Test `findPsiFile()` with valid path
- [ ] Test `findPsiFile()` with invalid path
- [ ] Test `offsetToElement()` with various offsets
- [ ] Test `lineColumnToOffset()` conversion
- [ ] Test `findRenameableElement()` navigation

**File**: `src/test/kotlin/io/hibob/intellijcontrolserver/util/PsiUtilsTest.kt`

### 6.2 Service Unit Tests
- [ ] Mock PSI elements
- [ ] Test rename with valid element
- [ ] Test rename with invalid element
- [ ] Test conflict detection
- [ ] Test error handling

**File**: `src/test/kotlin/io/hibob/intellijcontrolserver/services/RefactoringServiceTest.kt`

### 6.3 Integration Tests
- [ ] Create test Kotlin project fixture
- [ ] Test renaming class across files
- [ ] Test renaming method
- [ ] Test renaming variable
- [ ] Test renaming with conflicts
- [ ] Verify all references updated
- [ ] Verify file renames (if renaming file)

**File**: `src/test/kotlin/io/hibob/intellijcontrolserver/integration/RenameRefactoringTest.kt`

**Example Test**:
```kotlin
fun testRenameClass() {
    myFixture.configureByText("Service.kt", """
        class Service {
            fun doWork() {}
        }
    """)
    
    myFixture.configureByText("Controller.kt", """
        fun main() {
            val service = Service()
        }
    """)
    
    val result = refactoringService.rename(
        "Service.kt", 
        offset = 6, 
        newName = "EmployeeService"
    )
    
    assertThat(result.success).isTrue()
    assertThat(result.filesChanged).hasSize(2)
    assertThat(myFixture.file.text).contains("EmployeeService")
}
```

### 6.4 Manual Testing
- [ ] Create sample Kotlin project
- [ ] Add classes with cross-references
- [ ] Test renaming class via API
- [ ] Verify all references updated in IntelliJ
- [ ] Test renaming method
- [ ] Test renaming variable
- [ ] Test error cases (invalid offset, conflicts, etc.)

**Test Project Structure**:
```
test-project/
├── src/main/kotlin/
│   ├── Service.kt (class Service)
│   ├── Controller.kt (uses Service)
│   └── Main.kt (uses Service)
└── src/test/kotlin/
    └── ServiceTest.kt (tests Service)
```

**Manual Test Script**:
```bash
# Rename Service to EmployeeService
curl -X POST http://localhost:8765/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/Service.kt",
    "line": 1,
    "column": 7,
    "newName": "EmployeeService"
  }'

# Verify in IntelliJ:
# 1. Service.kt renamed to EmployeeService.kt
# 2. Class renamed in all files
# 3. All imports updated
```

## 7. Documentation

- [ ] Document `/refactor/rename` endpoint in API-SPEC.md
- [ ] Add usage examples
- [ ] Document error codes
- [ ] Add troubleshooting guide
- [ ] Update README with refactoring examples

## 8. Edge Cases

### 8.1 Renaming Files
- [ ] Handle when renaming class renames file
- [ ] Update file paths in response

### 8.2 Renaming Constructors
- [ ] Handle constructor renames (rename class instead)

### 8.3 Renaming Across Modules
- [ ] Test with multi-module projects
- [ ] Ensure all modules updated

### 8.4 Renaming with Tests
- [ ] Verify test classes updated (e.g., ServiceTest)

## Completion Checklist

- [ ] All tasks above completed
- [ ] `/refactor/rename` endpoint working
- [ ] PSI resolution working for offset and line/column
- [ ] Multi-file rename working
- [ ] Conflict detection working
- [ ] All error cases handled
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] Manual testing successful
- [ ] Documentation updated
- [ ] Ready for Phase 4

## Estimated Time

- Data models: 2 hours
- PSI utilities: 6 hours
- Service layer: 8 hours
- HTTP handler: 3 hours
- Error handling: 4 hours
- Testing: 10 hours
- Documentation: 2 hours
- Edge cases: 3 hours

**Total**: ~38 hours (5 days)

