# Architecture

## Overview

The IntelliJ Control Server is a plugin that embeds a lightweight HTTP server inside IntelliJ IDEA, exposing IDE operations through a RESTful JSON API.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     External Clients                        │
│  (Cursor AI, CLI tools, Scripts, Automation)                │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP/JSON
                            │ (localhost:8765)
┌───────────────────────────▼─────────────────────────────────┐
│                 IntelliJ Control Server Plugin              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              HTTP Server (Sun HttpServer)            │   │
│  └─────────────────────┬───────────────────────────────┘   │
│                        │                                     │
│  ┌─────────────────────▼───────────────────────────────┐   │
│  │           Request Router & Handlers                  │   │
│  │  • TasksHandler                                      │   │
│  │  • RefactoringHandler                                │   │
│  │  • NavigationHandler                                 │   │
│  │  • HealthHandler                                     │   │
│  └─────────────────────┬───────────────────────────────┘   │
│                        │                                     │
│  ┌─────────────────────▼───────────────────────────────┐   │
│  │              Service Layer                           │   │
│  │  • TaskManagerService                                │   │
│  │  • RefactoringService                                │   │
│  │  • NavigationService                                 │   │
│  └─────────────────────┬───────────────────────────────┘   │
│                        │                                     │
│  ┌─────────────────────▼───────────────────────────────┐   │
│  │          IntelliJ Platform APIs                      │   │
│  │  • TaskManager                                       │   │
│  │  • ContextManager                                    │   │
│  │  • RefactoringFactory                                │   │
│  │  • PsiManager                                        │   │
│  │  • FileEditorManager                                 │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Components

### 1. HTTP Server (`ControlServer`)

**Responsibility**: Accept and route HTTP requests

**Technology**: `com.sun.net.httpserver.HttpServer` (built into JDK)

**Key Features**:
- Starts on plugin initialization
- Stops on plugin disposal
- Binds to localhost only for security
- Configurable port (default: 8765)

**Implementation**:
```kotlin
class ControlServer : Disposable {
    private val server: HttpServer
    private val port: Int = 8765
    
    init {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
        registerHandlers()
        server.start()
    }
    
    private fun registerHandlers() {
        server.createContext("/health", HealthHandler())
        server.createContext("/tasks", TasksHandler())
        server.createContext("/refactor", RefactoringHandler())
        server.createContext("/navigation", NavigationHandler())
    }
}
```

### 2. Request Handlers

**Responsibility**: Parse requests, validate input, call services, format responses

**Pattern**: Each handler focuses on one domain (tasks, refactoring, navigation)

**Example Structure**:
```kotlin
class TasksHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val method = exchange.requestMethod
        val path = exchange.requestURI.path
        
        when {
            path == "/tasks/list" && method == "GET" -> handleList(exchange)
            path == "/tasks/current" && method == "GET" -> handleCurrent(exchange)
            path == "/tasks/switch" && method == "POST" -> handleSwitch(exchange)
            else -> sendError(exchange, 404, "Not found")
        }
    }
}
```

### 3. Service Layer

**Responsibility**: Bridge between HTTP handlers and IntelliJ APIs

**Key Services**:

#### TaskManagerService
```kotlin
class TaskManagerService(private val project: Project) {
    private val taskManager = TaskManager.getInstance(project)
    private val contextManager = ContextManager.getInstance(project)
    
    fun getAllTasks(): List<TaskInfo>
    fun getCurrentTask(): TaskInfo?
    fun switchTask(taskId: String): Boolean
    fun createTask(summary: String): TaskInfo
}
```

#### RefactoringService
```kotlin
class RefactoringService(private val project: Project) {
    fun renameElement(filePath: String, offset: Int, newName: String): RefactoringResult
    fun extractMethod(filePath: String, startOffset: Int, endOffset: Int, methodName: String): RefactoringResult
    fun moveClass(filePath: String, targetPackage: String): RefactoringResult
}
```

#### NavigationService
```kotlin
class NavigationService(private val project: Project) {
    fun openFile(filePath: String, line: Int? = null)
    fun jumpToDefinition(filePath: String, offset: Int)
    fun findUsages(filePath: String, offset: Int): List<UsageInfo>
}
```

### 4. IntelliJ Platform Integration

**Key APIs Used**:

- **TaskManager**: Manage IDE tasks
- **ContextManager**: Save/restore editor contexts
- **RefactoringFactory**: Create refactoring operations
- **PsiManager**: Parse and analyze code structure
- **FileEditorManager**: Open/navigate files
- **WriteCommandAction**: Execute write operations safely

## Threading Model

IntelliJ IDEA has strict threading requirements:

### Read Operations
- Must run on any thread
- Use `ReadAction.compute { }`

### Write Operations
- Must run on EDT (Event Dispatch Thread)
- Use `WriteCommandAction.runWriteCommandAction(project) { }`

### HTTP Server
- Handles requests on thread pool
- Must dispatch to correct thread for IDE operations

**Example**:
```kotlin
fun handleRename(request: RenameRequest): RenameResult {
    // HTTP thread -> Read thread -> EDT
    return ReadAction.compute<RenameResult, Throwable> {
        val element = findElementAt(request.filePath, request.offset)
        
        WriteCommandAction.runWriteCommandAction(project) {
            val refactoring = RenameRefactoring(project, element, request.newName)
            refactoring.run()
        }
        
        RenameResult(success = true)
    }
}
```

## Data Flow

### Example: Rename Refactoring

```
1. Client sends POST /refactor/rename
   {
     "filePath": "src/Service.kt",
     "offset": 150,
     "newName": "EmployeeService"
   }

2. HTTP Server receives request
   ↓
3. RefactoringHandler parses JSON, validates
   ↓
4. RefactoringService.renameElement() called
   ↓
5. ReadAction: Find PsiElement at offset
   ↓
6. WriteCommandAction: Execute rename refactoring
   ↓
7. RefactoringResult returned
   ↓
8. Handler formats JSON response
   {
     "success": true,
     "message": "Renamed to EmployeeService",
     "filesChanged": ["src/Service.kt", "src/ServiceTest.kt"]
   }
```

## Error Handling

**Levels**:

1. **Handler Level**: Validate input, catch exceptions
2. **Service Level**: Wrap platform exceptions
3. **Platform Level**: IntelliJ exceptions

**Response Format**:
```json
{
  "success": false,
  "error": {
    "code": "ELEMENT_NOT_FOUND",
    "message": "No renameable element found at offset 150",
    "details": {
      "filePath": "src/Service.kt",
      "offset": 150
    }
  }
}
```

## Configuration

**Config File**: `~/.intellij-control-server/config.json`

```kotlin
data class ServerConfig(
    val port: Int = 8765,
    val host: String = "127.0.0.1",
    val autoStart: Boolean = true,
    val logLevel: String = "INFO",
    val enableCors: Boolean = false
)
```

## Security Considerations

1. **Localhost Only**: Never expose to network
2. **No Authentication**: Assumes trusted local environment
3. **Input Validation**: Validate all file paths, offsets, names
4. **Path Traversal**: Ensure paths are within project
5. **Resource Limits**: Prevent DoS (rate limiting, request size limits)

## Performance

**Optimization Strategies**:

1. **Async Operations**: Don't block HTTP threads
2. **Caching**: Cache PSI lookups where safe
3. **Batch Operations**: Support multiple operations in one request
4. **Lazy Loading**: Only load data when needed

## Extension Points

**Future extensibility**:

1. **Plugin API**: Allow other plugins to register handlers
2. **Event Streams**: WebSocket support for IDE events
3. **Custom Refactorings**: Register custom refactoring operations
4. **Query Language**: SQL-like queries for code structure

## Dependencies

**Build Dependencies**:
- IntelliJ Platform SDK
- Kotlin stdlib
- Gson (JSON serialization)

**Runtime Dependencies**:
- IntelliJ Platform (provided)
- JDK 17+ (HttpServer)

## Plugin Lifecycle

```kotlin
class IntelliJControlServerPlugin : ApplicationComponent {
    private var server: ControlServer? = null
    
    override fun initComponent() {
        // Plugin loaded - start server
        server = ControlServer()
        logger.info("IntelliJ Control Server started on port 8765")
    }
    
    override fun disposeComponent() {
        // Plugin unloaded - stop server
        server?.stop()
        logger.info("IntelliJ Control Server stopped")
    }
}
```

## Testing Strategy

1. **Unit Tests**: Test service layer in isolation
2. **Integration Tests**: Test with IntelliJ test fixtures
3. **HTTP Tests**: Test API endpoints with mock HTTP client
4. **Manual Tests**: Real-world usage scenarios

## Logging

**Strategy**:
- Use IntelliJ's Logger
- Log all API requests (DEBUG level)
- Log errors (ERROR level)
- Log performance metrics (INFO level)

**Example**:
```kotlin
private val logger = Logger.getInstance(ControlServer::class.java)

logger.info("Handling request: POST /refactor/rename")
logger.debug("Request body: $requestBody")
logger.error("Failed to rename element", exception)
```

