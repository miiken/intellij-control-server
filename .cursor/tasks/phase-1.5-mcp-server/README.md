# Phase 1.5: MCP Server Support

## Overview

Add Model Context Protocol (MCP) support to enable native integration with AI clients like Cursor and Claude Desktop.

## Goal

Provide an MCP server alongside the existing HTTP REST API to offer:
- Self-describing tools with JSON schemas
- Native integration in AI coding assistants
- Streaming support for long operations
- Better discoverability and usability

## Architecture

### MCP vs HTTP REST API

```
┌─────────────────────────────┐
│   AI Client (Cursor)        │
└──────────┬──────────────────┘
           │
    ┌──────┴──────┐
    │             │
┌───▼────┐   ┌───▼─────┐
│  MCP   │   │  HTTP   │
│ Server │   │  REST   │
│ (stdio)│   │  API    │
└───┬────┘   └───┬─────┘
    │            │
    └────┬───────┘
         │
    ┌────▼─────────────┐
    │  IntelliJ IDEA   │
    │  PSI / Services  │
    └──────────────────┘
```

### MCP Protocol

- **Transport**: JSON-RPC 2.0 over stdio
- **Protocol Version**: 2024-11-05
- **Communication**: Request/Response + Notifications

### Key Methods

1. **initialize**: Client connects and negotiates capabilities
2. **tools/list**: Server advertises available tools
3. **tools/call**: Client executes a tool with parameters

## Features

### MCP Tools

#### 1. `intellij_health_check`
**Description**: Check server health and get status information

**Input Schema**:
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output**:
```json
{
  "status": "ok",
  "version": "1.0.0",
  "uptime": 3600,
  "timestamp": 1704124800000
}
```

#### 2. `intellij_rename_symbol`
**Description**: Rename a symbol (class, method, variable, parameter)

**Input Schema**:
```json
{
  "type": "object",
  "properties": {
    "projectName": {
      "type": "string",
      "description": "Name of the IntelliJ project"
    },
    "filePath": {
      "type": "string",
      "description": "Path to file (relative to project root)"
    },
    "line": {
      "type": "number",
      "description": "Line number (1-based)"
    },
    "oldName": {
      "type": "string",
      "description": "Current name of the symbol"
    },
    "newName": {
      "type": "string",
      "description": "New name for the symbol"
    }
  },
  "required": ["projectName", "filePath", "line", "oldName", "newName"]
}
```

**Output**:
```json
{
  "success": true,
  "filesChanged": ["src/Main.kt", "src/test/MainTest.kt"],
  "changesCount": 5
}
```

#### 3. `intellij_extract_method`
**Description**: Extract selected code into a new method

**Input Schema**:
```json
{
  "type": "object",
  "properties": {
    "projectName": {
      "type": "string",
      "description": "Name of the IntelliJ project"
    },
    "filePath": {
      "type": "string",
      "description": "Path to file (relative to project root)"
    },
    "startLine": {
      "type": "number",
      "description": "Start line of code selection (1-based)"
    },
    "endLine": {
      "type": "number",
      "description": "End line of code selection (1-based)"
    },
    "startColumn": {
      "type": "number",
      "description": "Start column on startLine (0-based, optional)"
    },
    "endColumn": {
      "type": "number",
      "description": "End column on endLine (0-based, optional)"
    },
    "methodName": {
      "type": "string",
      "description": "Name for the extracted method"
    },
    "visibility": {
      "type": "string",
      "enum": ["private", "protected", "public", "internal"],
      "description": "Visibility modifier for the method",
      "default": "private"
    }
  },
  "required": ["projectName", "filePath", "startLine", "endLine", "methodName"]
}
```

**Output**:
```json
{
  "success": true,
  "filesChanged": ["src/Service.kt"],
  "changesCount": 1
}
```

## Implementation Plan

### 1. Core Infrastructure
- [x] Create `McpServer` class (stdio JSON-RPC handler)
- [ ] Create `McpToolRegistry` for tool management
- [ ] Implement JSON-RPC 2.0 request/response handling
- [ ] Add error handling and validation

### 2. Tool Implementations
- [ ] `HealthCheckTool` - Server health status
- [ ] `RenameSymbolTool` - Rename refactoring
- [ ] Tool base interface and schemas

### 3. Integration
- [ ] Start MCP server alongside HTTP server
- [ ] Add MCP configuration to settings UI
- [ ] Lifecycle management (start/stop)

### 4. Configuration
- [ ] Enable/disable MCP server
- [ ] Configure stdio vs socket transport (future)
- [ ] Tool-specific settings

### 5. Testing & Documentation
- [ ] Test with Cursor MCP inspector
- [ ] Test with Claude Desktop
- [ ] Update user documentation
- [ ] Add MCP setup guide

## Configuration

### Settings UI

Add to existing settings page:

```
[x] Enable MCP Server
    Start MCP server on IntelliJ startup for AI tool integration

Available Tools:
  [x] intellij_health_check
  [x] intellij_rename_symbol
  [x] intellij_extract_method
```

### Cursor Configuration

Users add to `.cursor/mcp_config.json`:

```json
{
  "mcpServers": {
    "intellij-control-server": {
      "command": "path/to/intellij-mcp-launcher.sh",
      "args": []
    }
  }
}
```

## Testing

### Manual Testing with MCP Inspector

```bash
# Install MCP Inspector
npm install -g @modelcontextprotocol/inspector

# Test MCP server
mcp-inspector path/to/intellij-mcp-launcher.sh
```

### Integration Testing

1. Configure Cursor to use MCP server
2. Open a project in IntelliJ
3. Use Cursor to call tools:
   - Health check
   - Rename symbol
4. Verify operations complete successfully

## Benefits

### For Users
- ✅ Native tool selection in Cursor UI
- ✅ Self-documenting (schemas visible)
- ✅ Better error messages
- ✅ Type-safe parameters

### For Developers
- ✅ Standard protocol (JSON-RPC 2.0)
- ✅ Tool versioning support
- ✅ Streaming capabilities
- ✅ Works with multiple AI clients

## Compatibility

- **HTTP REST API**: Remains fully functional (not replaced)
- **Cursor**: Native MCP support
- **Claude Desktop**: Native MCP support
- **Other clients**: Can still use HTTP REST API

## Timeline

- **Estimated Duration**: 2-3 days
- **Target Release**: Include in v1.0.0

## References

- [MCP Specification](https://modelcontextprotocol.io/)
- [MCP GitHub](https://github.com/modelcontextprotocol)
- [Cursor MCP Documentation](https://docs.cursor.com/advanced/mcp)
- [JSON-RPC 2.0 Spec](https://www.jsonrpc.org/specification)

## Success Criteria

- [x] MCP server starts alongside HTTP server
- [ ] Tools are discoverable via `tools/list`
- [ ] Health check tool works
- [ ] Rename tool works with real projects
- [ ] Cursor can connect and use tools
- [ ] Documentation updated with MCP setup

