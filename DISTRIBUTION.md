# Distribution Guide

This guide explains how to build and distribute the IntelliJ Control Server plugin and MCP bridge.

## Overview

The project consists of two components:
1. **IntelliJ Plugin** - Installs into IntelliJ IDEA
2. **MCP Bridge** - Standalone CLI tool that connects Cursor to the plugin

## Building for Distribution

### 1. Build the IntelliJ Plugin

```bash
./gradlew buildPlugin
```

This creates a distributable ZIP file at:
```
build/distributions/intellij-control-server-1.0.0.zip
```

### 2. Build the MCP Bridge

```bash
./gradlew :mcp-bridge:distZip
```

This creates a distributable ZIP file at:
```
mcp-bridge/build/distributions/mcp-bridge-1.0.0.zip
```

### 3. Build Everything

```bash
./gradlew buildPlugin :mcp-bridge:distZip
```

---

## Installation Instructions for End Users

### Part 1: Install the IntelliJ Plugin

1. **Download** `intellij-control-server-1.0.0.zip`

2. **Install in IntelliJ IDEA:**
   - Open IntelliJ IDEA
   - Go to **Settings** → **Plugins** → ⚙️ (gear icon) → **Install Plugin from Disk...**
   - Select the downloaded `intellij-control-server-1.0.0.zip`
   - Restart IntelliJ IDEA

3. **Configure the Plugin (Optional):**
   - Go to **Settings** → **Tools** → **IntelliJ Control Server**
   - Set the port (default: 8767)
   - Enable MCP support (checkbox)
   - Enable auto-start (checkbox)

4. **Verify Installation:**
   - Open a project in IntelliJ
   - The plugin should start automatically
   - Test: `curl http://localhost:8767/health`

### Part 2: Install the MCP Bridge (for Cursor Integration)

#### Option A: Quick Install Script (Recommended)

**macOS/Linux:**
```bash
# Download and extract
curl -L https://github.com/miiken/intellij-control-server/releases/download/v1.0.0/mcp-bridge-1.0.0.zip -o mcp-bridge.zip
unzip mcp-bridge.zip -d ~/.intellij-mcp-bridge
rm mcp-bridge.zip

# Make executable
chmod +x ~/.intellij-mcp-bridge/mcp-bridge-1.0.0/bin/mcp-bridge

# Add to PATH (optional)
echo 'export PATH="$HOME/.intellij-mcp-bridge/mcp-bridge-1.0.0/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Windows (PowerShell):**
```powershell
# Download and extract
Invoke-WebRequest -Uri https://github.com/miiken/intellij-control-server/releases/download/v1.0.0/mcp-bridge-1.0.0.zip -OutFile mcp-bridge.zip
Expand-Archive -Path mcp-bridge.zip -DestinationPath $env:USERPROFILE\.intellij-mcp-bridge
Remove-Item mcp-bridge.zip

# Add to PATH (add this to your PowerShell profile)
$env:Path += ";$env:USERPROFILE\.intellij-mcp-bridge\mcp-bridge-1.0.0\bin"
```

#### Option B: Manual Install

1. **Download** `mcp-bridge-1.0.0.zip`

2. **Extract to a permanent location:**
   - macOS/Linux: `~/.intellij-mcp-bridge/`
   - Windows: `%USERPROFILE%\.intellij-mcp-bridge\`

3. **The executable will be at:**
   - macOS/Linux: `~/.intellij-mcp-bridge/mcp-bridge-1.0.0/bin/mcp-bridge`
   - Windows: `%USERPROFILE%\.intellij-mcp-bridge\mcp-bridge-1.0.0\bin\mcp-bridge.bat`

### Part 3: Configure Cursor

1. **Open Cursor Settings** (⌘+Shift+J or Ctrl+Shift+J)

2. **Add MCP Server Configuration:**

**macOS/Linux:**
```json
{
  "mcpServers": {
    "intellij": {
      "command": "/Users/YOUR_USERNAME/.intellij-mcp-bridge/mcp-bridge-1.0.0/bin/mcp-bridge",
      "args": ["http://127.0.0.1:8767"],
      "description": "IntelliJ IDEA refactoring tools"
    }
  }
}
```

**Windows:**
```json
{
  "mcpServers": {
    "intellij": {
      "command": "C:\\Users\\YOUR_USERNAME\\.intellij-mcp-bridge\\mcp-bridge-1.0.0\\bin\\mcp-bridge.bat",
      "args": ["http://127.0.0.1:8767"],
      "description": "IntelliJ IDEA refactoring tools"
    }
  }
}
```

**Tips:**
- Replace `YOUR_USERNAME` with your actual username
- Or use the full path from where you extracted the bridge
- If you changed the plugin's port in IntelliJ settings, update the port in `args`

3. **Restart Cursor** to load the MCP server

4. **Test the Integration:**
   - Open a project in both IntelliJ and Cursor
   - Ask Cursor: "Rename the variable `foo` to `bar` in Main.kt"
   - Cursor should use the IntelliJ plugin to perform the rename!

---

## Verification

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

### Check MCP Bridge
```bash
# macOS/Linux
~/.intellij-mcp-bridge/mcp-bridge-1.0.0/bin/mcp-bridge --version

# Or if added to PATH
mcp-bridge --version
```

### Test MCP Integration
```bash
echo '{"jsonrpc":"2.0","method":"tools/list","id":1}' | \
  ~/.intellij-mcp-bridge/mcp-bridge-1.0.0/bin/mcp-bridge http://localhost:8767
```

Should return a list of 3 tools: `intellij_health_check`, `intellij_rename_symbol`, `intellij_extract_method`

---

## Configuration

### Plugin Configuration File

Location: `~/.intellij-control-server/config.json`

```json
{
  "port": 8767,
  "host": "127.0.0.1",
  "autoStart": true,
  "enableMcp": true
}
```

You can manually edit this file to change settings without opening IntelliJ.

### Port Configuration

If port 8767 is already in use, you can change it:

1. **In IntelliJ:** Settings → Tools → IntelliJ Control Server → Port
2. **In config file:** Edit `~/.intellij-control-server/config.json`
3. **Update Cursor config:** Change the port in the `args` array

---

## Troubleshooting

### Plugin Not Starting

1. Check IntelliJ logs: **Help** → **Show Log in Finder/Explorer**
2. Search for "ControlServer" in the logs
3. Ensure port is not already in use: `lsof -i :8767` (macOS/Linux)

### MCP Bridge Can't Connect

1. Verify plugin is running: `curl http://localhost:8767/health`
2. Check the port in Cursor's MCP config matches the plugin's port
3. Ensure IntelliJ has a project open

### Cursor Can't Find MCP Bridge

1. Use the absolute path to the bridge executable
2. On Windows, make sure to use `.bat` extension
3. Check file permissions: `chmod +x ~/.intellij-mcp-bridge/mcp-bridge-1.0.0/bin/mcp-bridge`

### Tools Not Working

1. Wait for IntelliJ to finish indexing the project
2. Ensure the file paths are relative to the project root
3. Check IntelliJ logs for detailed error messages

---

## Publishing to JetBrains Marketplace (Future)

To publish the plugin to the official marketplace:

1. **Create JetBrains account** at https://plugins.jetbrains.com/

2. **Get plugin verification token:**
   ```bash
   export PUBLISH_TOKEN="your-token-here"
   ```

3. **Publish:**
   ```bash
   ./gradlew publishPlugin
   ```

4. **Users can then install directly** from IntelliJ:
   - Settings → Plugins → Marketplace
   - Search for "IntelliJ Control Server"
   - Click Install

---

## Release Checklist

Before releasing a new version:

- [ ] Update version in `build.gradle.kts` and `mcp-bridge/build.gradle.kts`
- [ ] Update `CHANGELOG.md` with new features and fixes
- [ ] Run all tests: `./gradlew test`
- [ ] Build both distributions: `./gradlew buildPlugin :mcp-bridge:distZip`
- [ ] Test the plugin installation manually
- [ ] Test the MCP bridge with Cursor
- [ ] Create GitHub release with both ZIP files
- [ ] Update README.md with new installation instructions
- [ ] Tag the release: `git tag v1.0.0 && git push origin v1.0.0`

---

## GitHub Release Creation

1. **Go to** https://github.com/miiken/intellij-control-server/releases/new

2. **Tag:** `v1.0.0`

3. **Release title:** `v1.0.0 - Initial Release`

4. **Upload artifacts:**
   - `build/distributions/intellij-control-server-1.0.0.zip`
   - `mcp-bridge/build/distributions/mcp-bridge-1.0.0.zip`

5. **Release notes:** Include features, installation instructions, and changelog

---

## Support

- **Issues:** https://github.com/miiken/intellij-control-server/issues
- **Documentation:** https://github.com/miiken/intellij-control-server
- **MCP Protocol:** https://modelcontextprotocol.io/

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.

