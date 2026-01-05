# Phase 1.5 Tasks: MCP Server Support

## Core Infrastructure

### Task 1.5.1: MCP Server Foundation
- [x] Create `McpServer` class
  - [x] JSON-RPC 2.0 request/response handling
  - [x] Stdio transport (read from stdin, write to stdout)
  - [x] Thread-safe operation
  - [ ] Error handling and validation
- [ ] Create `McpToolRegistry`
  - [ ] Tool registration
  - [ ] Tool lookup
  - [ ] Tool execution
- [ ] Implement MCP protocol methods
  - [ ] `initialize` - Capability negotiation
  - [ ] `tools/list` - List available tools
  - [ ] `tools/call` - Execute tool

### Task 1.5.2: Tool Interface
- [ ] Define `McpTool` interface
  - [ ] Tool metadata (name, description)
  - [ ] Input schema (JSON Schema)
  - [ ] Execute method
- [ ] Tool base classes and utilities
  - [ ] Schema validation helpers
  - [ ] Result formatting
  - [ ] Error handling

## Tool Implementations

### Task 1.5.3: Health Check Tool
- [x] Create `HealthCheckTool`
  - [x] Tool definition and schema
  - [ ] Implementation (call HTTP health endpoint)
  - [ ] Error handling
  - [ ] Tests

### Task 1.5.4: Rename Symbol Tool
- [ ] Create `RenameSymbolTool`
  - [ ] Tool definition and schema
  - [ ] Implementation (call RefactoringService)
  - [ ] Parameter validation
  - [ ] Error handling
  - [ ] Tests

## Integration

### Task 1.5.5: Lifecycle Management
- [ ] Start MCP server on IntelliJ startup
  - [ ] Integrate with `ControlServerApplicationListener`
  - [ ] Start in background thread
  - [ ] Handle errors gracefully
- [ ] Stop MCP server on IntelliJ shutdown
  - [ ] Clean shutdown
  - [ ] Close stdio streams
  - [ ] Terminate threads

### Task 1.5.6: Configuration
- [ ] Add MCP settings to `ServerConfig`
  - [ ] Enable/disable MCP server
  - [ ] Tool enable/disable flags
- [ ] Update `ControlServerConfigurable` UI
  - [ ] MCP server checkbox
  - [ ] Tool selection checkboxes
  - [ ] Help text and documentation links

## Launcher Script

### Task 1.5.7: MCP Launcher
- [ ] Create launcher script for Cursor
  - [ ] Shell script (macOS/Linux)
  - [ ] Batch script (Windows)
  - [ ] Find IntelliJ installation
  - [ ] Connect to running instance or start new
  - [ ] Handle errors

## Testing

### Task 1.5.8: Unit Tests
- [ ] Test `McpServer`
  - [ ] Request parsing
  - [ ] Response formatting
  - [ ] Error handling
- [ ] Test `McpToolRegistry`
  - [ ] Tool registration
  - [ ] Tool execution
- [ ] Test individual tools
  - [ ] `HealthCheckTool`
  - [ ] `RenameSymbolTool`

### Task 1.5.9: Integration Tests
- [ ] Test with MCP Inspector
  - [ ] Initialize connection
  - [ ] List tools
  - [ ] Call tools
- [ ] Test with Cursor
  - [ ] Configure MCP server
  - [ ] Use tools in chat
  - [ ] Verify results

## Documentation

### Task 1.5.10: User Documentation
- [ ] Update `README.md`
  - [ ] MCP overview
  - [ ] Benefits explanation
  - [ ] Setup instructions
- [ ] Create MCP setup guide
  - [ ] Cursor configuration
  - [ ] Claude Desktop configuration
  - [ ] Troubleshooting
- [ ] Update Swagger documentation
  - [ ] Note about MCP alternative
  - [ ] Link to MCP docs

### Task 1.5.11: Developer Documentation
- [ ] Document MCP architecture
  - [ ] How it works
  - [ ] Tool development guide
  - [ ] Testing guide
- [ ] Add comments to code
  - [ ] Class-level documentation
  - [ ] Method documentation
  - [ ] Example usage

## Release Preparation

### Task 1.5.12: Final Testing
- [ ] End-to-end testing
  - [ ] Start IntelliJ
  - [ ] MCP server starts automatically
  - [ ] Cursor can connect
  - [ ] All tools work correctly
- [ ] Error scenarios
  - [ ] IntelliJ not running
  - [ ] Invalid parameters
  - [ ] Project not found
- [ ] Performance testing
  - [ ] Response times
  - [ ] Memory usage
  - [ ] Thread safety

### Task 1.5.13: Release Checklist
- [ ] All tests passing
- [ ] Documentation complete
- [ ] Code reviewed
- [ ] Changelog updated
- [ ] Version bumped to 1.0.0 (or 1.1.0 if post-release)
- [ ] Plugin built and tested
- [ ] Ready for distribution

