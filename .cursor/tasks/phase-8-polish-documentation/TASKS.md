# Phase 6 Tasks

## 1. Error Handling

### 1.1 Standardize Error Responses
- [ ] Ensure all endpoints use consistent error format
- [ ] Create error code enum
- [ ] Standard HTTP status codes (400, 404, 500)
- [ ] Include request context in errors

**Standard Format**:
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {
      "field": "value",
      "suggestion": "How to fix"
    }
  }
}
```

### 1.2 Global Exception Handler
- [ ] Create `ExceptionHandler` utility
- [ ] Catch all unhandled exceptions
- [ ] Log full stack traces
- [ ] Return safe error responses
- [ ] Never expose internal details

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/server/ExceptionHandler.kt`

### 1.3 Error Code Documentation
- [ ] Document all error codes
- [ ] Provide resolution steps
- [ ] Include examples

**Error Codes**:
- `INVALID_REQUEST`
- `FILE_NOT_FOUND`
- `ELEMENT_NOT_FOUND`
- `TASK_NOT_FOUND`
- `REFACTORING_FAILED`
- `PERMISSION_DENIED`
- `SERVER_ERROR`

## 2. Input Validation

### 2.1 Request Validator
- [ ] Create `RequestValidator` object
- [ ] Validate file paths (no path traversal)
- [ ] Validate offsets (non-negative, within bounds)
- [ ] Validate names (legal identifiers)
- [ ] Validate required fields

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/validation/RequestValidator.kt`

```kotlin
object RequestValidator {
    fun validateFilePath(path: String, project: Project): ValidationResult
    fun validateOffset(offset: Int, fileLength: Int): ValidationResult
    fun validateIdentifier(name: String): ValidationResult
    fun validatePackageName(packageName: String): ValidationResult
}
```

### 2.2 Validation Rules
- [ ] File paths must be within project
- [ ] Offsets must be non-negative
- [ ] Names must be valid identifiers
- [ ] Required fields must be present
- [ ] Numeric ranges validated

### 2.3 Validation Messages
- [ ] Clear error messages
- [ ] Suggest corrections
- [ ] Include valid examples

## 3. Performance Optimization

### 3.1 Profiling
- [ ] Profile all endpoints with JProfiler or VisualVM
- [ ] Identify slow operations
- [ ] Measure memory usage
- [ ] Find bottlenecks

### 3.2 PSI Caching
- [ ] Cache PSI file lookups (with invalidation)
- [ ] Cache project structure queries
- [ ] Implement LRU cache for hot paths

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/cache/PsiCache.kt`

### 3.3 Async Operations
- [ ] Make long operations async where possible
- [ ] Use CompletableFuture for non-blocking ops
- [ ] Add timeout handling

### 3.4 Resource Management
- [ ] Ensure all streams closed
- [ ] Clean up PSI references
- [ ] Dispose resources properly
- [ ] Monitor memory usage

### 3.5 Performance Benchmarks
- [ ] Create benchmark tests
- [ ] Measure endpoint latencies
- [ ] Track memory footprint
- [ ] Set performance baselines

**Targets**:
- Health check: < 10ms
- Task list: < 100ms
- Task switch: < 200ms
- Rename: < 2s
- Find usages: < 5s

## 4. Configuration UI

### 4.1 Settings Configurable
- [ ] Create `ControlServerSettingsConfigurable`
- [ ] Implement IntelliJ settings interface
- [ ] Create settings panel UI
- [ ] Save/load settings

**File**: `src/main/kotlin/io/miiken/intellijcontrolserver/ui/SettingsConfigurable.kt`

### 4.2 Settings Panel
- [ ] Port number field
- [ ] Host binding field (with warning for non-localhost)
- [ ] Auto-start checkbox
- [ ] Log level dropdown
- [ ] CORS enable checkbox (with warning)
- [ ] Server status indicator

**UI Components**:
```kotlin
class ControlServerSettingsPanel {
    private val portField: JBTextField
    private val hostField: JBTextField
    private val autoStartCheckbox: JBCheckBox
    private val logLevelComboBox: ComboBox<String>
    private val enableCorsCheckbox: JBCheckBox
    private val statusLabel: JBLabel
}
```

### 4.3 Settings Persistence
- [ ] Use IntelliJ's PersistentStateComponent
- [ ] Save to IntelliJ settings
- [ ] Load on plugin init
- [ ] Apply changes without restart (if possible)

### 4.4 Validation in UI
- [ ] Validate port range (1024-65535)
- [ ] Warn on non-localhost binding
- [ ] Warn on CORS enable
- [ ] Check port availability

## 5. Documentation

### 5.1 README.md
- [ ] Clear project description
- [ ] Quick start guide
- [ ] Installation instructions
- [ ] Basic usage examples
- [ ] Link to detailed docs

### 5.2 API Reference
- [ ] Complete endpoint documentation
- [ ] Request/response examples
- [ ] Error codes reference
- [ ] Authentication info (none, but document this)
- [ ] Rate limiting info (none, but document this)

**File**: `docs/API-REFERENCE.md`

### 5.3 User Guide
- [ ] How to install
- [ ] How to configure
- [ ] How to use with Cursor
- [ ] Common workflows
- [ ] Tips and tricks

**File**: `docs/USER-GUIDE.md`

### 5.4 Troubleshooting Guide
- [ ] Common issues and solutions
- [ ] How to check server status
- [ ] How to view logs
- [ ] How to report bugs
- [ ] FAQ

**File**: `docs/TROUBLESHOOTING.md`

### 5.5 Contributing Guide
- [ ] How to set up dev environment
- [ ] How to build and test
- [ ] Coding standards
- [ ] How to submit PRs
- [ ] Code of conduct

**File**: `CONTRIBUTING.md`

### 5.6 CHANGELOG
- [ ] Document all changes by version
- [ ] Follow Keep a Changelog format
- [ ] Include breaking changes
- [ ] Include bug fixes
- [ ] Include new features

**File**: `CHANGELOG.md`

## 6. Client Examples

### 6.1 Python Client
- [ ] Create `intellij_client.py` module
- [ ] Class-based API wrapper
- [ ] Type hints
- [ ] Error handling
- [ ] Usage examples
- [ ] README

**File**: `examples/python/intellij_client.py`

```python
class IntelliJClient:
    def __init__(self, base_url="http://localhost:8765"):
        self.base_url = base_url
    
    def health(self) -> dict:
        """Check server health"""
        
    def list_tasks(self) -> List[Task]:
        """List all tasks"""
        
    def rename(self, file_path: str, offset: int, new_name: str) -> RefactoringResult:
        """Rename element"""
```

### 6.2 Node.js Client
- [ ] Create `intellij-client.js` module
- [ ] TypeScript definitions
- [ ] Promise-based API
- [ ] Error handling
- [ ] Usage examples
- [ ] README

**File**: `examples/nodejs/intellij-client.js`

```javascript
class IntelliJClient {
  constructor(baseURL = 'http://localhost:8765') {
    this.client = axios.create({ baseURL });
  }
  
  async health() {
    const response = await this.client.get('/health');
    return response.data;
  }
  
  async rename(filePath, offset, newName) {
    const response = await this.client.post('/refactor/rename', {
      filePath, offset, newName
    });
    return response.data;
  }
}
```

### 6.3 Bash/CLI Commands
- [ ] Create bash function library
- [ ] Convenient aliases
- [ ] jq integration for JSON parsing
- [ ] Usage examples
- [ ] README

**File**: `examples/bash/ij-commands.sh`

```bash
#!/bin/bash

IJ_BASE_URL="http://localhost:8765"

ij-health() {
  curl -s "$IJ_BASE_URL/health" | jq
}

ij-tasks-list() {
  curl -s "$IJ_BASE_URL/tasks/list" | jq
}

ij-rename() {
  local file="$1"
  local offset="$2"
  local new_name="$3"
  curl -s -X POST "$IJ_BASE_URL/refactor/rename" \
    -H "Content-Type: application/json" \
    -d "{\"filePath\":\"$file\",\"offset\":$offset,\"newName\":\"$new_name\"}" | jq
}
```

### 6.4 cURL Examples
- [ ] Document cURL commands for all endpoints
- [ ] Include headers
- [ ] Include request bodies
- [ ] Show expected responses

## 7. Testing & Quality

### 7.1 End-to-End Tests
- [ ] Test complete workflows
- [ ] Test error scenarios
- [ ] Test concurrent requests
- [ ] Test with large codebases

### 7.2 Performance Tests
- [ ] Measure all endpoint latencies
- [ ] Stress test with many requests
- [ ] Memory leak detection
- [ ] Long-running stability test

### 7.3 Code Quality
- [ ] Run ktlint
- [ ] Fix all linter warnings
- [ ] Add KDoc comments
- [ ] Review code coverage

### 7.4 Security Review
- [ ] Validate input sanitization
- [ ] Review path traversal prevention
- [ ] Review localhost-only binding
- [ ] Document security considerations

## 8. Beta Testing

### 8.1 Internal Testing
- [ ] Test with your team
- [ ] Collect feedback
- [ ] Fix critical bugs
- [ ] Iterate on UX

### 8.2 Public Beta
- [ ] Release to beta testers
- [ ] Set up feedback channel (GitHub issues)
- [ ] Monitor for crashes
- [ ] Quick bug fixes

### 8.3 Bug Fixes
- [ ] Triage reported bugs
- [ ] Fix high-priority issues
- [ ] Regression testing
- [ ] Update documentation

## 9. Plugin Marketplace Preparation

### 9.1 Plugin Descriptor
- [ ] Complete plugin.xml metadata
- [ ] Add description
- [ ] Add change notes
- [ ] Add vendor info
- [ ] Add icon

### 9.2 Plugin Listing
- [ ] Create marketplace listing
- [ ] Write compelling description
- [ ] Add screenshots
- [ ] Add video demo (optional)
- [ ] Set categories/tags

### 9.3 Legal & Licensing
- [ ] Add LICENSE file (MIT)
- [ ] Add copyright notices
- [ ] Review third-party licenses

### 9.4 Version Management
- [ ] Set version to 1.0.0
- [ ] Tag release in git
- [ ] Build final artifact
- [ ] Test installation from zip

## 10. Release

### 10.1 Pre-Release Checklist
- [ ] All tests passing
- [ ] Documentation complete
- [ ] CHANGELOG updated
- [ ] Version bumped to 1.0.0
- [ ] Plugin built: `./gradlew buildPlugin`
- [ ] Manual testing in clean environment
- [ ] Beta testers approved

### 10.2 Release Process
- [ ] Tag v1.0.0 in git
- [ ] Build final plugin
- [ ] Upload to JetBrains Marketplace
- [ ] Create GitHub release
- [ ] Update README with marketplace link

### 10.3 Announcement
- [ ] Write release blog post
- [ ] Announce on social media
- [ ] Announce in IntelliJ community
- [ ] Email beta testers

### 10.4 Post-Release
- [ ] Monitor marketplace ratings
- [ ] Monitor GitHub issues
- [ ] Quick bug fixes if needed
- [ ] Plan v1.1.0 features

## Completion Checklist

- [ ] All tasks above completed
- [ ] Error handling comprehensive
- [ ] Input validation working
- [ ] Performance optimized
- [ ] Configuration UI working
- [ ] Documentation complete
- [ ] Client examples available
- [ ] Beta testing complete
- [ ] Plugin published to marketplace
- [ ] v1.0.0 released
- [ ] Ready for v2.0 planning

## Estimated Time

- Error handling: 4 hours
- Input validation: 4 hours
- Performance optimization: 8 hours
- Configuration UI: 6 hours
- Documentation: 12 hours
- Client examples: 8 hours
- Testing & QA: 8 hours
- Beta testing: 16 hours (over time)
- Marketplace prep: 4 hours
- Release process: 4 hours

**Total**: ~74 hours (9 days, some parallel with beta testing)

