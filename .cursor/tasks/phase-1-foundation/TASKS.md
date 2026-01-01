# Phase 1 Tasks

## 1. Project Setup

### 1.1 Gradle Configuration
- [ ] Configure `build.gradle.kts` with IntelliJ Platform plugin
- [ ] Set target IntelliJ version (2023.3+)
- [ ] Add Gson dependency
- [ ] Configure Kotlin version
- [ ] Set compatibility ranges

**Acceptance Criteria**:
- `./gradlew buildPlugin` succeeds
- Plugin ZIP created in `build/distributions/`

### 1.2 Plugin Descriptor
- [ ] Create/update `plugin.xml` with metadata
- [ ] Define plugin ID: `io.miiken.intellij-control-server`
- [ ] Set plugin name and description
- [ ] Configure application service
- [ ] Set compatibility version range

**Acceptance Criteria**:
- Plugin loads in IntelliJ sandbox: `./gradlew runIde`

## 2. Configuration System

### 2.1 Config Data Model
- [ ] Create `ServerConfig` data class
- [ ] Fields: `port`, `host`, `autoStart`, `logLevel`, `enableCors`
- [ ] Default values defined

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/config/ServerConfig.kt`

### 2.2 Config Loading
- [ ] Create `ConfigLoader` service
- [ ] Load from `~/.intellij-control-server/config.json`
- [ ] Fall back to defaults if file missing
- [ ] Validate configuration values

**Acceptance Criteria**:
- Config loads from file when present
- Defaults used when file missing
- Invalid configs logged and use defaults

## 3. HTTP Server

### 3.1 Server Implementation
- [ ] Create `ControlServer` class
- [ ] Initialize `HttpServer` on given port/host
- [ ] Bind to `127.0.0.1` only
- [ ] Start server in `init` block
- [ ] Implement `Disposable` for cleanup

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/ControlServer.kt`

### 3.2 Handler Registration
- [ ] Create handler registration system
- [ ] Register `/health` context
- [ ] Set up base error handling
- [ ] Configure thread pool

**Acceptance Criteria**:
- Server starts on plugin load
- Server accessible on `http://localhost:8765`
- Server stops on plugin unload

## 4. Health Check Endpoint

### 4.1 Handler Implementation
- [ ] Create `HealthHandler` implementing `HttpHandler`
- [ ] Parse GET request
- [ ] Return JSON response with status, version, uptime
- [ ] Set correct Content-Type header

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/server/handlers/HealthHandler.kt`

**Response Format**:
```json
{
  "status": "ok",
  "version": "1.0.0",
  "uptime": 3600
}
```

### 4.2 Response Utilities
- [ ] Create `ResponseBuilder` utility
- [ ] Helper for JSON responses
- [ ] Helper for error responses
- [ ] Set standard headers (Content-Type, CORS)

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/server/ResponseBuilder.kt`

## 5. Logging Infrastructure

### 5.1 Logger Setup
- [ ] Create logger instances using IntelliJ Logger
- [ ] Log server start/stop
- [ ] Log all incoming requests (DEBUG level)
- [ ] Log errors with stack traces

### 5.2 Request Logging
- [ ] Log request method and path
- [ ] Log request processing time
- [ ] Log response status codes

**Acceptance Criteria**:
- Logs visible in IntelliJ log file
- `tail -f ~/Library/Logs/JetBrains/IntelliJIdea*/idea.log | grep ControlServer`

## 6. Error Handling

### 6.1 Error Response Format
- [ ] Define standard error response structure
- [ ] Include `success: false`, `error` object
- [ ] Include error code, message, optional details

**Format**:
```json
{
  "success": false,
  "error": {
    "code": "SERVER_ERROR",
    "message": "Internal server error",
    "details": {}
  }
}
```

### 6.2 Exception Handling
- [ ] Catch all handler exceptions
- [ ] Return 500 status with error JSON
- [ ] Log full exception details
- [ ] Don't expose internal details to client

## 7. Plugin Lifecycle

### 7.1 Application Service
- [ ] Create plugin application service
- [ ] Initialize `ControlServer` on component init
- [ ] Dispose server on component disposal
- [ ] Handle initialization errors gracefully

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/IntelliJControlServerPlugin.kt`

### 7.2 Startup Notification
- [ ] Log plugin version on startup
- [ ] Log server URL
- [ ] Show notification balloon (optional)

## 8. Testing

### 8.1 Manual Testing
- [ ] Run `./gradlew runIde`
- [ ] Test health check: `curl http://localhost:8765/health`
- [ ] Verify JSON response
- [ ] Check logs for startup messages
- [ ] Close IntelliJ, verify server stops

### 8.2 Unit Tests
- [ ] Test `ConfigLoader` with valid/invalid configs
- [ ] Test `HealthHandler` response format
- [ ] Test `ResponseBuilder` utilities

**File**: `src/test/kotlin/io/miiken/intellijcontrolserver/config/ConfigLoaderTest.kt`

## 9. Documentation

- [ ] Update README.md with build instructions
- [ ] Document configuration options
- [ ] Add troubleshooting section
- [ ] Create CHANGELOG.md entry

## Completion Checklist

- [ ] All tasks above completed
- [ ] `./gradlew buildPlugin` succeeds
- [ ] `./gradlew runIde` starts sandbox IntelliJ
- [ ] Health check returns correct JSON
- [ ] Logs show server lifecycle events
- [ ] Server stops cleanly on IntelliJ exit
- [ ] Ready for Phase 2

## Estimated Time

- Setup: 2 hours
- Config system: 2 hours
- HTTP server: 3 hours
- Health check: 1 hour
- Logging: 1 hour
- Testing: 2 hours
- Documentation: 1 hour

**Total**: ~12 hours (1.5 days)

