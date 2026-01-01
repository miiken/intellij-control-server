# Phase 5 Tasks

## 1. Data Models

### 1.1 Navigation Request Models
- [ ] Create `OpenFileRequest` data class
- [ ] Create `JumpToDefinitionRequest` data class
- [ ] Create `FindUsagesRequest` data class
- [ ] Support both offset and line/column positions

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/model/NavigationRequest.kt`

```kotlin
data class OpenFileRequest(
    val filePath: String,
    val line: Int? = null,
    val column: Int? = null
)

data class JumpToDefinitionRequest(
    val filePath: String,
    val offset: Int? = null,
    val line: Int? = null,
    val column: Int? = null
)

data class FindUsagesRequest(
    val filePath: String,
    val offset: Int? = null,
    val line: Int? = null,
    val column: Int? = null
)
```

### 1.2 Navigation Response Models
- [ ] Create `NavigationResult` data class
- [ ] Create `DefinitionInfo` data class
- [ ] Create `UsageInfo` data class
- [ ] Create `ProjectStructure` data class

```kotlin
data class DefinitionInfo(
    val filePath: String,
    val line: Int,
    val column: Int,
    val elementType: String,
    val elementName: String
)

data class UsageInfo(
    val filePath: String,
    val line: Int,
    val column: Int,
    val snippet: String
)

data class FindUsagesResult(
    val success: Boolean,
    val element: ElementInfo,
    val usages: List<UsageInfo>,
    val totalUsages: Int
)
```

## 2. Service Layer

### 2.1 NavigationService Implementation
- [ ] Create `NavigationService` class
- [ ] Constructor takes `Project` parameter
- [ ] Implement `openFile()` method
- [ ] Implement `jumpToDefinition()` method
- [ ] Implement `findUsages()` method
- [ ] Implement `getProjectStructure()` method

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/services/NavigationService.kt`

```kotlin
class NavigationService(private val project: Project) {
    fun openFile(filePath: String, line: Int?, column: Int?): NavigationResult
    fun jumpToDefinition(filePath: String, offset: Int): DefinitionInfo?
    fun findUsages(filePath: String, offset: Int): FindUsagesResult
    fun getProjectStructure(depth: Int, includeTests: Boolean): ProjectStructure
}
```

### 2.2 Open File Implementation
- [ ] Use `FileEditorManager` to open file
- [ ] Convert file path to VirtualFile
- [ ] Open file in editor
- [ ] Navigate to line/column if specified
- [ ] Focus the editor

**Implementation**:
```kotlin
fun openFile(filePath: String, line: Int?, column: Int?): NavigationResult {
    return ReadAction.compute<NavigationResult, Throwable> {
        val virtualFile = LocalFileSystem.getInstance()
            .findFileByPath(project.basePath + "/" + filePath)
            ?: throw FileNotFoundException(filePath)
        
        ApplicationManager.getApplication().invokeLater {
            val fileEditorManager = FileEditorManager.getInstance(project)
            val editor = fileEditorManager.openFile(virtualFile, true).firstOrNull()
            
            if (line != null && editor is TextEditor) {
                val position = LogicalPosition(line - 1, column ?: 0)
                editor.editor.caretModel.moveToLogicalPosition(position)
                editor.editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
            }
        }
        
        NavigationResult(success = true, filePath = filePath)
    }
}
```

**Acceptance Criteria**:
- Opens file in editor
- Navigates to specified line/column
- Scrolls to make line visible
- Focuses the editor
- Handles missing files gracefully

### 2.3 Jump to Definition Implementation
- [ ] Find element at offset
- [ ] Get element's declaration
- [ ] Use `GotoDeclarationAction` or resolve manually
- [ ] Open target file
- [ ] Navigate to definition
- [ ] Return definition info

**Implementation**:
```kotlin
fun jumpToDefinition(filePath: String, offset: Int): DefinitionInfo? {
    return ReadAction.compute<DefinitionInfo?, Throwable> {
        val psiFile = PsiUtils.findPsiFile(project, filePath)
        val element = psiFile.findElementAt(offset)
        
        val targetElement = element?.reference?.resolve()
            ?: return@compute null
        
        val targetFile = targetElement.containingFile
        val targetOffset = targetElement.textOffset
        val targetLine = targetFile.viewProvider.document
            ?.getLineNumber(targetOffset)?.plus(1) ?: 0
        
        // Navigate in UI thread
        ApplicationManager.getApplication().invokeLater {
            openFile(targetFile.virtualFile.path, targetLine, null)
        }
        
        DefinitionInfo(
            filePath = targetFile.virtualFile.path,
            line = targetLine,
            column = 0,
            elementType = targetElement.javaClass.simpleName,
            elementName = (targetElement as? PsiNamedElement)?.name ?: ""
        )
    }
}
```

**Acceptance Criteria**:
- Resolves references correctly
- Navigates to definition location
- Returns definition metadata
- Handles unresolved references

### 2.4 Find Usages Implementation
- [ ] Find element at offset
- [ ] Use `FindUsagesManager` to find usages
- [ ] Collect usage locations
- [ ] Extract code snippets around usages
- [ ] Return usage list

**Implementation**:
```kotlin
fun findUsages(filePath: String, offset: Int): FindUsagesResult {
    return ReadAction.compute<FindUsagesResult, Throwable> {
        val psiFile = PsiUtils.findPsiFile(project, filePath)
        val element = psiFile.findElementAt(offset)
        val targetElement = element?.parent as? PsiNamedElement
            ?: throw ElementNotFoundException(offset)
        
        val usageFinder = FindUsagesManager(project)
        val handler = usageFinder.getFindUsagesHandler(targetElement, false)
        val usages = mutableListOf<UsageInfo>()
        
        handler?.processElementUsages(targetElement, { usage ->
            val usageElement = usage.element ?: return@processElementUsages true
            val usageFile = usageElement.containingFile
            val usageOffset = usageElement.textOffset
            val document = usageFile.viewProvider.document
            val line = document?.getLineNumber(usageOffset)?.plus(1) ?: 0
            
            // Extract snippet (e.g., 50 chars around usage)
            val snippet = extractSnippet(document, usageOffset, 50)
            
            usages.add(UsageInfo(
                filePath = usageFile.virtualFile.path,
                line = line,
                column = 0,
                snippet = snippet
            ))
            
            true // continue processing
        }, FindUsagesOptions(project))
        
        FindUsagesResult(
            success = true,
            element = ElementInfo(
                type = targetElement.javaClass.simpleName,
                name = targetElement.name ?: ""
            ),
            usages = usages,
            totalUsages = usages.size
        )
    }
}
```

**Acceptance Criteria**:
- Finds all usages across project
- Includes usage locations
- Extracts meaningful code snippets
- Handles elements with no usages

### 2.5 Project Structure Implementation
- [ ] Traverse project directories
- [ ] Collect packages and files
- [ ] Respect depth parameter
- [ ] Filter test sources if requested
- [ ] Build hierarchical structure

**Implementation**:
```kotlin
fun getProjectStructure(depth: Int = 3, includeTests: Boolean = true): ProjectStructure {
    return ReadAction.compute<ProjectStructure, Throwable> {
        val structure = mutableMapOf<String, Any>()
        
        val contentRoots = ProjectRootManager.getInstance(project)
            .contentSourceRoots
        
        for (root in contentRoots) {
            if (!includeTests && root.path.contains("test")) {
                continue
            }
            
            traverseDirectory(root, structure, depth)
        }
        
        ProjectStructure(
            project = project.name,
            structure = structure
        )
    }
}

private fun traverseDirectory(
    dir: VirtualFile, 
    result: MutableMap<String, Any>, 
    depthRemaining: Int
) {
    if (depthRemaining <= 0) return
    
    val files = mutableListOf<String>()
    val subpackages = mutableListOf<String>()
    
    dir.children.forEach { child ->
        if (child.isDirectory) {
            subpackages.add(child.name)
        } else {
            files.add(child.name)
        }
    }
    
    result["files"] = files
    if (subpackages.isNotEmpty()) {
        result["subpackages"] = subpackages
    }
}
```

**Acceptance Criteria**:
- Returns hierarchical structure
- Respects depth limit
- Filters by test/main sources
- Handles empty directories

## 3. HTTP Handlers

### 3.1 NavigationHandler Base
- [ ] Create `NavigationHandler` implementing `HttpHandler`
- [ ] Route based on path
- [ ] Handle GET and POST methods
- [ ] Standard error handling

**File**: `src/main/kotlin/io/hibob/intellijcontrolserver/server/handlers/NavigationHandler.kt`

### 3.2 POST /navigation/open-file
- [ ] Parse `OpenFileRequest`
- [ ] Validate file path
- [ ] Call `navigationService.openFile()`
- [ ] Return result

**Request**:
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "line": 42,
  "column": 15
}
```

**Response**:
```json
{
  "success": true,
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "fileOpened": true,
  "cursorPosition": {"line": 42, "column": 15}
}
```

### 3.3 POST /navigation/jump-to-definition
- [ ] Parse `JumpToDefinitionRequest`
- [ ] Call `navigationService.jumpToDefinition()`
- [ ] Return definition info

**Request**:
```json
{
  "filePath": "src/main/kotlin/Controller.kt",
  "offset": 450
}
```

**Response**:
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

### 3.4 POST /navigation/find-usages
- [ ] Parse `FindUsagesRequest`
- [ ] Call `navigationService.findUsages()`
- [ ] Return usage list

**Request**:
```json
{
  "filePath": "src/main/kotlin/EmployeeService.kt",
  "offset": 150
}
```

**Response**:
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

### 3.5 GET /navigation/project-structure
- [ ] Parse query parameters (depth, includeTests)
- [ ] Call `navigationService.getProjectStructure()`
- [ ] Return project structure

**Request**:
```
GET /navigation/project-structure?depth=3&includeTests=true
```

**Response**:
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

## 4. Utilities

### 4.1 Code Snippet Extraction
- [ ] Create `extractSnippet()` utility
- [ ] Extract code around offset
- [ ] Trim to meaningful boundaries (e.g., line boundaries)
- [ ] Handle edge cases (start/end of file)

```kotlin
fun extractSnippet(document: Document?, offset: Int, contextChars: Int): String {
    if (document == null) return ""
    
    val start = maxOf(0, offset - contextChars)
    val end = minOf(document.textLength, offset + contextChars)
    
    val text = document.getText(TextRange(start, end))
    return text.trim()
}
```

### 4.2 Virtual File Utilities
- [ ] Add to `PsiUtils`: `pathToVirtualFile()`
- [ ] Add to `PsiUtils`: `virtualFileToPath()`
- [ ] Handle absolute vs relative paths

## 5. Error Handling

### 5.1 File Not Found
- [ ] Return `FILE_NOT_FOUND` error

### 5.2 No Definition Found
- [ ] Return `DEFINITION_NOT_FOUND` error
- [ ] Include element details

### 5.3 No Usages Found
- [ ] Return success with empty usages list
- [ ] Not an error condition

### 5.4 Invalid Depth
- [ ] Validate depth parameter > 0
- [ ] Return `INVALID_REQUEST` error

## 6. Testing

### 6.1 Open File Tests
- [ ] Test opening existing file
- [ ] Test opening with line/column
- [ ] Test opening non-existent file
- [ ] Test cursor positioning

### 6.2 Jump to Definition Tests
- [ ] Test jumping to class definition
- [ ] Test jumping to method definition
- [ ] Test jumping to variable definition
- [ ] Test unresolved references

### 6.3 Find Usages Tests
- [ ] Test finding method usages
- [ ] Test finding class usages
- [ ] Test finding variable usages
- [ ] Test element with no usages
- [ ] Test usage snippet extraction

### 6.4 Project Structure Tests
- [ ] Test with different depths
- [ ] Test with/without tests
- [ ] Test empty directories
- [ ] Test nested packages

### 6.5 Integration Tests
- [ ] Create test project with references
- [ ] Test full navigation flow
- [ ] Verify files open in IntelliJ
- [ ] Verify cursor positions

**File**: `src/test/kotlin/io/hibob/intellijcontrolserver/integration/NavigationTest.kt`

### 6.6 Manual Testing
- [ ] Create sample project
- [ ] Test opening files via API
- [ ] Test jumping to definitions
- [ ] Test finding usages
- [ ] Verify in IntelliJ UI

## 7. Documentation

- [ ] Document all endpoints in API-SPEC.md
- [ ] Add usage examples
- [ ] Document query parameters
- [ ] Update README

## Completion Checklist

- [ ] All tasks above completed
- [ ] All 4 endpoints working
- [ ] Open file working
- [ ] Jump to definition working
- [ ] Find usages working
- [ ] Project structure working
- [ ] Error handling comprehensive
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] Manual testing successful
- [ ] Documentation updated
- [ ] Ready for Phase 6

## Estimated Time

- Data models: 2 hours
- Open file: 3 hours
- Jump to definition: 4 hours
- Find usages: 5 hours
- Project structure: 4 hours
- HTTP handlers: 3 hours
- Utilities: 2 hours
- Error handling: 2 hours
- Testing: 6 hours
- Documentation: 2 hours

**Total**: ~33 hours (4 days)

