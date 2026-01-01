# IntelliJ Control Server

A lightweight IntelliJ IDEA plugin that exposes IDE capabilities via a local HTTP API.

## Quick Links

📋 **Planning & Documentation**: See `.cursor/tasks/planning/`
- [README](.cursor/tasks/planning/README.md) - Full project overview
- [ARCHITECTURE](.cursor/tasks/planning/ARCHITECTURE.md) - Technical design
- [API-SPEC](.cursor/tasks/planning/API-SPEC.md) - API reference
- [IMPLEMENTATION-PLAN](.cursor/tasks/planning/IMPLEMENTATION-PLAN.md) - Development roadmap
- [USE-CASES](.cursor/tasks/planning/USE-CASES.md) - Real-world examples

## What is This?

This plugin runs a local HTTP server inside IntelliJ IDEA, allowing external tools (like Cursor AI, CLI tools, or scripts) to:

- **Manage Tasks & Contexts** - Switch between tasks with automatic context restoration
- **Trigger Refactorings** - Rename, extract method, move class, etc.
- **Navigate Code** - Open files, jump to definitions, find usages

## Quick Start

```bash
# Build the plugin
./gradlew buildPlugin

# Install in IntelliJ
# Settings → Plugins → ⚙️ → Install Plugin from Disk
# Select: build/distributions/intellij-control-server-1.0.0.zip

# Verify it's running
curl http://localhost:8765/health
```

## Example Usage

```bash
# List all tasks
curl http://localhost:8765/tasks/list | jq

# Rename a class
curl -X POST http://localhost:8765/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/Service.kt",
    "offset": 150,
    "newName": "EmployeeService"
  }'
```

## Project Status

🚧 **In Planning Phase** - Not yet implemented

See [IMPLEMENTATION-PLAN](.cursor/tasks/planning/IMPLEMENTATION-PLAN.md) for current development status.

## License

MIT License
