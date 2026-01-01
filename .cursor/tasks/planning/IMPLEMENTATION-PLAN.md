# Implementation Plan

## Development Phases

### Phase 1: Foundation (Week 1)

**Goal**: Basic infrastructure and health check

**Tasks**:
- [x] Project structure setup
- [ ] Gradle build configuration
- [ ] Plugin.xml configuration
- [ ] Basic HTTP server implementation
- [ ] Health check endpoint
- [ ] Logging infrastructure
- [ ] Configuration loading
- [ ] Basic error handling

**Deliverable**: Plugin that starts HTTP server and responds to `/health`

**Test**: `curl http://localhost:8765/health` returns `{"status":"ok"}`

---

### Phase 2: Tasks API (Week 2)

**Goal**: Complete task management functionality

**Tasks**:
- [ ] TaskManagerService implementation
- [ ] GET `/tasks/list` endpoint
- [ ] GET `/tasks/current` endpoint
- [ ] POST `/tasks/switch` endpoint
- [ ] POST `/tasks/create` endpoint
- [ ] POST `/tasks/save-context` endpoint
- [ ] Context saving/restoration logic
- [ ] Error handling for task operations
- [ ] Unit tests for TaskManagerService
- [ ] Integration tests with IntelliJ test framework

**Deliverable**: Full task management via API

**Test Scenarios**:
```bash
# List tasks
curl http://localhost:8765/tasks/list

# Create task
curl -X POST http://localhost:8765/tasks/create \
  -d '{"summary":"Test task"}'

# Switch task
curl -X POST http://localhost:8765/tasks/switch \
  -d '{"taskId":"test-task"}'
```

---

### Phase 3: Basic Refactoring (Week 3-4)

**Goal**: Rename refactoring (most common operation)

**Tasks**:
- [ ] RefactoringService skeleton
- [ ] PSI element resolution (offset → element)
- [ ] POST `/refactor/rename` endpoint
- [ ] Rename processor integration
- [ ] Preview support (optional)
- [ ] Conflict detection
- [ ] Multi-file refactoring support
- [ ] Unit tests for RefactoringService
- [ ] Integration tests with sample projects

**Deliverable**: Rename refactoring working for classes, methods, variables

**Test Scenarios**:
```bash
# Rename a method
curl -X POST http://localhost:8765/refactor/rename \
  -d '{
    "filePath":"src/main/kotlin/Service.kt",
    "offset":150,
    "newName":"EmployeeService"
  }'
```

---

### Phase 4: Advanced Refactoring (Week 5-6)

**Goal**: Extract method, extract variable, move class

**Tasks**:
- [ ] POST `/refactor/extract-method` endpoint
- [ ] Selection handling (start/end offsets)
- [ ] Method signature generation
- [ ] POST `/refactor/extract-variable` endpoint
- [ ] Expression analysis
- [ ] POST `/refactor/move-class` endpoint
- [ ] Package validation
- [ ] Import updates
- [ ] POST `/refactor/inline` endpoint
- [ ] Unit tests for each refactoring
- [ ] Integration tests

**Deliverable**: Complete refactoring toolkit

---

### Phase 5: Navigation API (Week 7)

**Goal**: File navigation and code browsing

**Tasks**:
- [ ] NavigationService implementation
- [ ] POST `/navigation/open-file` endpoint
- [ ] POST `/navigation/jump-to-definition` endpoint
- [ ] POST `/navigation/find-usages` endpoint
- [ ] GET `/navigation/project-structure` endpoint
- [ ] Offset/line/column conversion utilities
- [ ] Unit tests
- [ ] Integration tests

**Deliverable**: Complete navigation functionality

---

### Phase 6: Polish & Documentation (Week 8)

**Goal**: Production-ready plugin

**Tasks**:
- [ ] Comprehensive error handling
- [ ] Input validation for all endpoints
- [ ] Performance optimization
- [ ] Memory leak prevention
- [ ] Configuration UI in IntelliJ settings
- [ ] User documentation
- [ ] API client examples (Python, Node.js, Bash)
- [ ] Plugin marketplace preparation
- [ ] Beta testing with real users

**Deliverable**: Publishable plugin v1.0.0

---

## Technical Milestones

### Milestone 1: MVP (End of Week 4)
- ✅ HTTP server running
- ✅ Health check
- ✅ Tasks API complete
- ✅ Rename refactoring working

**Success Criteria**: Can manage tasks and rename symbols via API

### Milestone 2: Feature Complete (End of Week 7)
- ✅ All refactoring operations
- ✅ Navigation API
- ✅ Full test coverage

**Success Criteria**: All documented API endpoints working

### Milestone 3: Release Candidate (End of Week 8)
- ✅ Documentation complete
- ✅ No known critical bugs
- ✅ Performance validated

**Success Criteria**: Ready for public release

---

## Development Environment Setup

### Prerequisites

1. **IntelliJ IDEA Ultimate** (for plugin development)
2. **JDK 17+**
3. **Gradle 8.0+**

### Initial Setup

```bash
# Clone repository
git clone https://github.com/yourusername/intellij-control-server
cd intellij-control-server

# Build plugin
./gradlew buildPlugin

# Run in sandbox IntelliJ
./gradlew runIde
```

### Development Workflow

1. **Make changes** in `src/main/kotlin`
2. **Run tests**: `./gradlew test`
3. **Test manually**: `./gradlew runIde` (opens sandbox IntelliJ)
4. **Build plugin**: `./gradlew buildPlugin`
5. **Install in real IntelliJ**: Install from `build/distributions/*.zip`

---

## Testing Strategy

### Unit Tests

**Location**: `src/test/kotlin`

**Coverage Target**: 80%+

**Focus**:
- Service layer logic
- Request parsing
- Error handling
- Data transformations

**Example**:
```kotlin
class TaskManagerServiceTest {
    @Test
    fun `GIVEN task exists WHEN switching task THEN context restored`() {
        // Arrange
        val service = TaskManagerService(mockProject)
        
        // Act
        val result = service.switchTask("test-task")
        
        // Assert
        assertThat(result.success).isTrue()
        assertThat(result.contextRestored).isTrue()
    }
}
```

### Integration Tests

**Framework**: IntelliJ Platform Test Framework

**Coverage**: End-to-end API calls with real PSI

**Example**:
```kotlin
class RefactoringIntegrationTest : BasePlatformTestCase() {
    fun testRenameClass() {
        myFixture.configureByText("Service.kt", """
            class Service { }
        """)
        
        val result = refactoringService.rename("Service.kt", 6, "EmployeeService")
        
        assertThat(result.success).isTrue()
        assertThat(myFixture.file.text).contains("EmployeeService")
    }
}
```

### Manual Testing

**Test Project**: Create sample Kotlin/Java project

**Test Scenarios**:
- Create task, switch tasks, verify context
- Rename class across multiple files
- Extract method with complex selection
- Move class to different package

---

## Architecture Decisions

### Why HttpServer over WebSocket?

**Pros**:
- Simpler implementation
- RESTful design (familiar to developers)
- Stateless (easier to debug)
- No connection management needed

**Cons**:
- Can't push events to clients
- Higher latency for frequent operations

**Decision**: Start with HTTP, add WebSocket in v2.0 if needed

### Why Gson over Jackson?

**Pros**:
- Lighter weight
- Simpler API
- Sufficient for our needs

**Cons**:
- Less feature-rich

**Decision**: Use Gson, can switch later if needed

### Why No Authentication?

**Pros**:
- Simpler implementation
- Localhost-only reduces risk
- No token management

**Cons**:
- Any local process can control IDE

**Decision**: No auth in v1.0, add in v2.0 if users request

---

## Risk Management

### Risk: IntelliJ API Changes

**Likelihood**: Medium  
**Impact**: High  
**Mitigation**: 
- Target latest stable IntelliJ version
- Set wide compatibility range (233-241.*)
- Monitor IntelliJ release notes

### Risk: Threading Issues

**Likelihood**: High  
**Impact**: Critical  
**Mitigation**:
- Strict use of ReadAction/WriteCommandAction
- Comprehensive concurrency testing
- Detailed logging of thread issues

### Risk: Performance Degradation

**Likelihood**: Medium  
**Impact**: Medium  
**Mitigation**:
- Performance benchmarks
- Async operations where possible
- Request size limits

---

## Future Enhancements (v2.0+)

### WebSocket Support
- Real-time IDE events (file changes, compilation, etc.)
- Bidirectional communication

### Advanced Queries
- SQL-like queries for code structure
- Complex search operations

### Batch Operations
- Multiple refactorings in one request
- Transaction-like semantics

### Code Generation
- Generate boilerplate code
- Template-based generation

### Debugging Integration
- Set/remove breakpoints
- Start debug sessions
- Query debug state

### Build Integration
- Trigger builds
- Run tests
- View build results

---

## Contributing Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use IntelliJ formatter (ktlint)
- Document public APIs

### Commit Messages
- Use conventional commits format
- Include issue numbers

### Pull Requests
- Include tests for new features
- Update documentation
- Ensure CI passes

### Issue Reporting
- Use issue templates
- Include reproduction steps
- Attach IntelliJ logs if applicable

---

## Release Process

### Version Numbering

**Format**: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking API changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes

### Release Checklist

- [ ] All tests passing
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Version number bumped
- [ ] Plugin built: `./gradlew buildPlugin`
- [ ] Manual testing in clean environment
- [ ] Tag release in Git
- [ ] Publish to JetBrains Marketplace

### Beta Testing

- Internal testing with your team
- Public beta via JetBrains Marketplace (Alpha/Beta channel)
- Collect feedback via GitHub issues

---

## Resources

### IntelliJ Platform SDK
- [Plugin Development](https://plugins.jetbrains.com/docs/intellij/)
- [PSI Cookbook](https://plugins.jetbrains.com/docs/intellij/psi-cookbook.html)
- [Threading Model](https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html)

### Community
- [Plugin Development Forum](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development)
- [Slack: #intellij-platform](https://plugins.jetbrains.com/slack)

### Example Plugins
- [Google Cloud Tools](https://github.com/GoogleCloudPlatform/google-cloud-intellij)
- [Kotlin Plugin](https://github.com/JetBrains/kotlin/tree/master/idea)

