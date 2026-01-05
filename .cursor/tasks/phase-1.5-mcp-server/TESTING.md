# MCP Integration Testing Guide

## Overview

The IntelliJ Control Server now supports the Model Context Protocol (MCP), allowing AI tools like Cursor to interact with IntelliJ IDEA through a standardized protocol.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Cursor AI                         │
└────────────┬────────────────────────────────────────┘
             │ JSON-RPC 2.0 over stdio
             ▼
┌─────────────────────────────────────────────────────┐
│              MCP Bridge (standalone)                │
│  • Reads from stdin                                 │
│  • Makes HTTP calls to plugin                       │
│  • Writes to stdout                                 │
│  • Zero tool knowledge                              │
└────────────┬────────────────────────────────────────┘
             │ HTTP
             ▼
┌─────────────────────────────────────────────────────┐
│         IntelliJ Plugin (runs in IDE)               │
│  • McpController: /mcp/tools, /mcp/call             │
│  • ToolRegistry: self-registering tools             │
│  • 3 tools: health, rename, extract                 │
└─────────────────────────────────────────────────────┘
```

## Available Tools

1. **intellij_health_check** - Check if the IntelliJ Control Server is running
2. **intellij_rename_symbol** - Rename a symbol (class, method, variable, parameter)
3. **intellij_extract_method** - Extract selected code into a new method

## Manual Testing

### 1. Build the Plugin and Bridge

```bash
cd /Users/avner.linder/IdeaProjects/intellij-control-server
./gradlew build
./gradlew :mcp-bridge:installDist
```

### 2. Start the Sandbox IDE

```bash
./gradlew runIde
```

Wait for the server to start (check the logs for "IntelliJ Control Server started").

### 3. Test HTTP Endpoints

```bash
# List all available tools
curl http://127.0.0.1:8767/mcp/tools | python3 -m json.tool

# Call a specific tool
curl -X POST http://127.0.0.1:8767/mcp/call \
  -H "Content-Type: application/json" \
  -d '{"name":"intellij_health_check","arguments":{}}'
```

### 4. Test MCP Bridge (Standalone)

```bash
# Test tools/list
echo '{"jsonrpc":"2.0","method":"tools/list","id":1}' | \
  ./mcp-bridge/build/install/mcp-bridge/bin/mcp-bridge http://127.0.0.1:8767

# Test tools/call
echo '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"intellij_health_check","arguments":{}},"id":2}' | \
  ./mcp-bridge/build/install/mcp-bridge/bin/mcp-bridge http://127.0.0.1:8767
```

## Cursor Configuration

Add this to your Cursor settings (⌘+Shift+J → "cursor settings"):

```json
{
  "mcpServers": {
    "intellij": {
      "command": "/Users/avner.linder/IdeaProjects/intellij-control-server/mcp-bridge/build/install/mcp-bridge/bin/mcp-bridge",
      "args": ["http://127.0.0.1:8767"]
    }
  }
}
```

**Note:** Adjust the port number in `args` if your plugin is configured to use a different port. Check `~/.intellij-control-server/config.json` for your current configuration.

## Usage in Cursor

Once configured, you can ask Cursor to perform IntelliJ operations:

- "Rename the variable 'greeting' in Main.kt to 'welcomeMessage'"
- "Extract lines 5-8 into a new method called 'processData'"
- "Check if the IntelliJ server is running"

Cursor will automatically use the MCP integration to communicate with your IntelliJ IDE.

## Troubleshooting

### Bridge can't connect to plugin

- Check if the IntelliJ plugin is running
- Verify the port number matches in both:
  - Cursor configuration: `args: ["http://127.0.0.1:8767"]`
  - Plugin configuration: `~/.intellij-control-server/config.json`
- Test the HTTP endpoint directly: `curl http://127.0.0.1:8767/health`

### Tools not appearing

- Check the IDE logs for ToolRegistry initialization
- Verify all tool classes are in `io.miiken.intellijcontrolserver.mcp.tools` package
- Ensure each tool has a companion object with self-registration

### Refactoring operations failing

- Ensure the project is open in IntelliJ
- Check that the file paths are relative to the project root
- Verify line numbers and symbol names are correct
- Review the IDE logs for detailed error messages

## Test Results

### ✅ HTTP Endpoints
- `GET /mcp/tools` → Returns 3 tools with full schemas
- `POST /mcp/call` → Executes tools successfully

### ✅ MCP Bridge
- JSON-RPC 2.0 protocol working correctly
- `tools/list` returns all registered tools
- `tools/call` executes health check successfully

### ✅ Tool Discovery
- Tools self-register via companion object init blocks
- ToolRegistry uses classpath scanning
- All 3 tools discovered and exposed

## Adding New Tools

To add a new MCP tool:

1. Create a new class in `src/main/kotlin/io/miiken/intellijcontrolserver/mcp/tools/`
2. Implement the `Tool<IN, OUT>` interface
3. Add a companion object with self-registration:

```kotlin
companion object {
    init {
        ToolRegistry.register(MyNewTool())
    }
}
```

4. Rebuild the plugin - the tool will automatically be discovered and exposed!

No changes needed to the bridge or any central registry.

