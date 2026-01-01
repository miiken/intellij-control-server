# IntelliJ Control Server

A lightweight IntelliJ IDEA plugin that exposes IDE capabilities via a local HTTP API, enabling programmatic control from any client (CLI tools, AI assistants, automation scripts, etc.).

## Overview

This plugin runs a local HTTP server inside IntelliJ IDEA, allowing external tools to:
- Manage IntelliJ Tasks & Contexts
- Trigger refactoring operations
- Control IDE navigation and state
- Query project information

**Not tied to any specific client** - works with Cursor, command-line tools, scripts, or any HTTP client.

## Features

### Tasks & Context Management
- List all tasks with details
- Switch between tasks (with automatic context switching)
- Create tasks from descriptions
- Get current task information
- Save/restore editor contexts

### Refactoring Operations
- Rename symbols (classes, methods, variables, files)
- Extract method from selection
- Extract variable from expression
- Move class/file to different package
- Inline method/variable
- Change method signature
- Introduce parameter

### Navigation & Information
- Open files at specific lines
- Jump to definition
- Find usages
- Get symbol information at cursor position
- List project structure

## Quick Start

### Installation

1. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```

2. Install in IntelliJ:
   - Go to Settings → Plugins → ⚙️ → Install Plugin from Disk
   - Select `build/distributions/intellij-control-server-1.0.0.zip`
   - Restart IntelliJ

3. Verify server is running:
   ```bash
   curl http://localhost:8765/health
   # Response: {"status":"ok","version":"1.0.0"}
   ```

### Usage Examples

**From command line:**
```bash
# List tasks
curl http://localhost:8765/tasks/list | jq

# Switch task
curl -X POST http://localhost:8765/tasks/switch \
  -H "Content-Type: application/json" \
  -d '{"taskId": "work-change-type-diff"}'

# Rename a class
curl -X POST http://localhost:8765/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/Service.kt",
    "offset": 150,
    "newName": "EmployeeService"
  }'
```

**From a script:**
```python
import requests

# Get current task
response = requests.get("http://localhost:8765/tasks/current")
current_task = response.json()
print(f"Working on: {current_task['summary']}")

# Open a file
requests.post("http://localhost:8765/navigation/open-file", json={
    "filePath": "src/main/kotlin/Employee.kt",
    "line": 42
})
```

## Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Technical architecture and design decisions
- **[API-SPEC.md](API-SPEC.md)** - Complete API reference with examples
- **[IMPLEMENTATION-PLAN.md](IMPLEMENTATION-PLAN.md)** - Development roadmap and phases
- **[USE-CASES.md](USE-CASES.md)** - Real-world usage scenarios

## Configuration

Default configuration works out of the box. Optional config file: `~/.intellij-control-server/config.json`

```json
{
  "port": 8765,
  "host": "127.0.0.1",
  "autoStart": true,
  "logLevel": "INFO",
  "enableCors": false
}
```

## Security

- **Local only**: Server binds to `127.0.0.1` (not accessible from network)
- **No authentication**: Designed for trusted local environment
- **File system access**: Restricted to project workspace
- **Read/Write operations**: Requires project to be open in IntelliJ

## Client Libraries

While the server uses plain HTTP/JSON, client libraries can make integration easier:

- **Bash/CLI**: Use `curl` or `httpie`
- **Python**: Use `requests` library
- **Node.js**: Use `axios` or `fetch`
- **Custom CLI**: See `cli/` directory for reference implementation

## Use Cases

- **AI-assisted development**: Let AI tools control IntelliJ refactoring
- **Automation scripts**: Automate repetitive refactoring tasks
- **Custom workflows**: Build tools that integrate with your IDE
- **Testing tools**: Programmatically set up test scenarios
- **Documentation generation**: Extract IDE context for documentation

## Requirements

- IntelliJ IDEA 2023.3 or later (Community or Ultimate)
- Java 17 or later
- Gradle 8.0 or later (for building)

## Troubleshooting

**Server not starting:**
```bash
# Check if port is in use
lsof -i :8765

# View plugin logs
tail -f ~/Library/Logs/JetBrains/IntelliJIdea*/idea.log | grep ControlServer
```

**Connection refused:**
- Ensure IntelliJ is running
- Check plugin is enabled: Settings → Plugins → IntelliJ Control Server
- Check firewall isn't blocking localhost connections

**Refactoring fails:**
- Ensure file is part of an open project
- Check file is not read-only
- Verify symbol exists at specified offset

## Development

See [IMPLEMENTATION-PLAN.md](IMPLEMENTATION-PLAN.md) for development setup and contribution guidelines.

## License

MIT License - See LICENSE file

## Contributing

Contributions welcome! Focus areas:
- Additional refactoring operations
- Better error handling and validation
- Performance optimizations
- Client library implementations
