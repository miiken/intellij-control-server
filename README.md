# IntelliJ Control Server

**A lightweight IntelliJ IDEA plugin that exposes IDE capabilities via HTTP API and MCP protocol.**

Enable AI tools like Cursor to perform IntelliJ refactorings, navigate code, and manage tasks directly from your AI assistant!

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/miiken/intellij-control-server/releases)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![IntelliJ](https://img.shields.io/badge/IntelliJ-2024.3+-orange.svg)](https://www.jetbrains.com/idea/)

---

## ✨ Features

- 🔄 **Refactoring** - Rename symbols, extract methods with IntelliJ's powerful refactoring engine
- 🤖 **MCP Integration** - Native support for Model Context Protocol (Cursor, Claude Desktop, etc.)
- 🌐 **HTTP API** - RESTful API with Swagger documentation for custom integrations
- ⚙️ **Configurable** - Adjust port, enable/disable features via Settings UI
- 🚀 **Auto-start** - Starts automatically when IntelliJ opens

---

## 📦 Installation

### Super Simple! 3 Steps:

#### 1️⃣ Install the Plugin

**Option A: From JetBrains Marketplace** (Coming Soon)
- In IntelliJ: **Settings** → **Plugins** → **Marketplace**
- Search for "IntelliJ Control Server"
- Click **Install**

**Option B: From GitHub Release**
1. Download `intellij-control-server-1.0.0.zip` from [Releases](https://github.com/miiken/intellij-control-server/releases/latest)
2. In IntelliJ: **Settings** → **Plugins** → ⚙️ → **Install Plugin from Disk**
3. Select the downloaded ZIP file
4. Restart IntelliJ IDEA

#### 2️⃣ Copy the Config (Appears Automatically!)

After restart, IntelliJ will show a notification with the Cursor configuration. Just copy it!

The config looks like this (FIXED for everyone):

```json
{
  "mcpServers": {
    "intellij": {
      "command": "/Users/YOUR_USERNAME/.intellij-mcp-bridge/bridge.sh"
    }
  }
}
```

> 🎯 Replace `YOUR_USERNAME` with your actual username (e.g., `john` or `avner.linder`)

#### 3️⃣ Add to Cursor & Restart

1. Open Cursor settings: **⌘+Shift+J** (Mac) or **Ctrl+Shift+J** (Windows/Linux)
2. Paste the config
3. Restart Cursor

**That's it!** 🎉 The plugin automatically installs the MCP bridge when IntelliJ starts.

---

### What Happens Automatically

When you first start IntelliJ after installing:
- ✅ Plugin auto-starts the HTTP server (port 8767)
- ✅ Plugin auto-installs the MCP bridge to `~/.intellij-mcp-bridge/bridge.sh`
- ✅ Plugin shows a notification with the exact Cursor config to copy
- ✅ You just paste and restart Cursor - done!

---

## 🚀 Usage

### With Cursor (via MCP)

Simply ask Cursor to perform IntelliJ operations:

```
"Rename the variable 'userName' in Main.kt to 'greeting'"
"Extract lines 10-15 into a new method called 'validateInput'"
"Check if the IntelliJ server is running"
```

Cursor will automatically use the IntelliJ plugin to perform these refactorings!

### Direct HTTP API

```bash
# Check server status
curl http://localhost:8767/health

# View API documentation
open http://localhost:8767/api-docs

# Rename a symbol
curl -X POST http://localhost:8767/my-project/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/Main.kt",
    "line": 10,
    "oldName": "userName",
    "newName": "greeting"
  }'

# Extract method
curl -X POST http://localhost:8767/my-project/refactor/extract-method \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/Main.kt",
    "startLine": 10,
    "endLine": 15,
    "methodName": "validateInput"
  }'
```

---

## 🛠️ Configuration

### IntelliJ Plugin Settings

**Settings** → **Tools** → **IntelliJ Control Server**

- **Port**: HTTP server port (default: 8767)
- **Host**: Bind address (default: 127.0.0.1)
- **Auto-start**: Start server when IntelliJ opens
- **Enable MCP**: Enable MCP protocol support

### Configuration File

Location: `~/.intellij-control-server/config.json`

```json
{
  "port": 8767,
  "host": "127.0.0.1",
  "autoStart": true,
  "enableMcp": true
}
```

---

## 📚 Documentation

- **[DISTRIBUTION.md](DISTRIBUTION.md)** - Complete distribution and installation guide
- **[DEVELOPMENT.md](DEVELOPMENT.md)** - Development setup and testing
- **[API Documentation](http://localhost:8767/api-docs)** - Interactive Swagger docs (when server is running)
- **[MCP Integration](.cursor/tasks/phase-1.5-mcp-server/README.md)** - MCP protocol details

### Available Tools (MCP)

| Tool | Description |
|------|-------------|
| `intellij_health_check` | Check if the IntelliJ server is running |
| `intellij_rename_symbol` | Rename a class, method, variable, or parameter |
| `intellij_extract_method` | Extract selected code into a new method |

More tools coming in future releases!

---

## 🔍 Verification

### Check Plugin Status
```bash
curl http://localhost:8767/health
```

Expected response:
```json
{
  "status": "ok",
  "version": "1.0.0",
  "uptime": 123,
  "timestamp": 1234567890
}
```

### Test MCP Bridge
```bash
# macOS/Linux
echo '{"jsonrpc":"2.0","method":"tools/list","id":1}' | \
  ~/.intellij-mcp-bridge/mcp-bridge-1.0.0/bin/mcp-bridge http://localhost:8767

# Should return list of 3 tools
```

---

## 🗺️ Roadmap

### v1.0 (Current) - Foundation & Basic Refactoring ✅
- HTTP server with health check
- Swagger/OpenAPI documentation
- Rename refactoring
- Extract method refactoring
- MCP protocol support
- Cursor integration

### v1.1 (Planned) - Tasks API
- Create/list/switch tasks
- Automatic context switching
- Task-aware file navigation

### v1.2 (Planned) - Advanced Refactoring
- Move class/method
- Change signature
- Inline method/variable
- Extract interface

### v1.3 (Planned) - Navigation API
- Find usages
- Go to definition
- Find implementations
- Search everywhere

### v2.0 (Future) - Enhanced AI Integration
- Code analysis tools
- Batch operations
- Project-wide refactorings

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

```bash
git clone https://github.com/miiken/intellij-control-server.git
cd intellij-control-server
./gradlew buildPlugin
./gradlew runIde  # Launches sandbox IDE for testing
```

See [DEVELOPMENT.md](DEVELOPMENT.md) for detailed development instructions.

---

## 🐛 Troubleshooting

### Plugin Not Starting

1. Check IntelliJ logs: **Help** → **Show Log in Finder/Explorer**
2. Search for "ControlServer" in the logs
3. Ensure port is not already in use: `lsof -i :8767` (macOS/Linux) or `netstat -ano | findstr :8767` (Windows)

### MCP Bridge Can't Connect

1. Verify plugin is running: `curl http://localhost:8767/health`
2. Check the port in Cursor's MCP config matches the plugin's port
3. Ensure IntelliJ has a project open

### Refactoring Not Working

1. Wait for IntelliJ to finish indexing the project
2. Ensure file paths are relative to the project root
3. Verify the line number and symbol name are correct
4. Check IntelliJ logs for detailed error messages

For more help, see [DISTRIBUTION.md](DISTRIBUTION.md#troubleshooting) or [open an issue](https://github.com/miiken/intellij-control-server/issues).

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Built with [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- MCP protocol by [Anthropic](https://modelcontextprotocol.io/)
- Inspired by AI-powered development workflows

---

## 📬 Contact

- **Issues**: [GitHub Issues](https://github.com/miiken/intellij-control-server/issues)
- **Email**: avner.linder@gmail.com
- **Author**: [@miiken](https://github.com/miiken)

---

**Made with ❤️ for developers who want AI to understand their IntelliJ projects**
