# Development Guide

## Quick Start

### Running the Plugin in Development

The **fastest way** to test your plugin changes:

```bash
# Set up Gradle 8 (required)
export PATH="/opt/homebrew/opt/gradle@8/bin:$PATH"

# Launch sandbox IntelliJ with plugin pre-installed
gradle runIde
```

This opens a **new IntelliJ window** with your plugin already installed. No manual installation needed!

### Development Workflow

```bash
# 1. Make code changes in your editor
vim src/main/kotlin/...

# 2. Run in sandbox
gradle runIde

# 3. Test in the sandbox IntelliJ that opens
# The plugin will be running with your latest changes

# 4. Close sandbox, make more changes, repeat
```

### Build for Distribution

When ready to install in your main IntelliJ:

```bash
gradle buildPlugin
# Creates: build/distributions/intellij-control-server-1.0.0.zip
```

Then install via: `Settings → Plugins → Install from Disk`

## Gradle Tasks Reference

| Task | Purpose | Use Case |
|------|---------|----------|
| `gradle runIde` | Launch sandbox IntelliJ | **Development & Testing** |
| `gradle buildPlugin` | Build distribution ZIP | Production installation |
| `gradle test` | Run unit tests | Verify code |
| `gradle verifyPlugin` | Validate plugin.xml | Pre-release checks |

## Testing the Plugin

### 1. In Sandbox (Development)
```bash
# Terminal 1: Run sandbox
gradle runIde

# Terminal 2: Test endpoint once sandbox starts
curl http://127.0.0.1:8765/health
```

### 2. Check Logs
Sandbox logs are separate from your main IntelliJ:
```bash
# Sandbox logs location
~/Library/Logs/JetBrains/IdeaIC{version}/idea-sandbox/system/log/idea.log
```

Look for:
```
INFO - ControlServerService - ControlServerService initialized
INFO - ControlServer - ✓ IntelliJ Control Server started successfully
```

### 3. Configuration
In the sandbox IntelliJ:
- Go to `Settings → Tools → IntelliJ Control Server`
- Configure port, host, etc.
- Changes are saved to sandbox's config directory

## Common Issues

### Port Already in Use
If you see "Address already in use" errors:

```bash
# Check what's using the port
lsof -i :8765

# Kill it or change the port in settings
```

### Plugin Not Loading
1. Check `build/distributions/` - ZIP should exist
2. Look at sandbox logs for errors
3. Verify `plugin.xml` is valid: `gradle verifyPlugin`

### Code Changes Not Reflected
- Close the sandbox IntelliJ completely
- Run `gradle runIde` again (full rebuild)
- For faster iteration, use `gradle runIde --no-build-cache`

## Hot Reload (Advanced)

For even faster development, enable JVM hotswap:

1. In sandbox IntelliJ, go to `Settings → Build → Compiler`
2. Enable "Build project automatically"
3. Make code changes
4. Press `Cmd + Shift + F9` (Recompile)
5. Plugin reloads without restarting IntelliJ

**Note**: Only works for method body changes, not structural changes

## Debugging

### Enable Debug Mode
```bash
gradle runIde --debug-jvm
```

Then attach your main IntelliJ's debugger to port 5005:
1. `Run → Edit Configurations → + → Remote JVM Debug`
2. Set port to `5005`
3. Set breakpoints in plugin code
4. Start debugging session

### Increase Log Verbosity
In sandbox IntelliJ:
- `Help → Diagnostic Tools → Debug Log Settings`
- Add: `io.miiken.intellijcontrolserver`
- Restart sandbox

## Performance Tips

### Speed Up Builds
```bash
# Skip tests during development
gradle runIde -x test

# Use build cache
gradle runIde --build-cache

# Parallel builds (if you have multiple modules)
gradle runIde --parallel
```

### Faster Sandbox Startup
Add to `gradle.properties`:
```properties
# Reduce plugin verification time
org.gradle.caching=true
org.gradle.parallel=true
```

## Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   └── io/miiken/intellijcontrolserver/
│   │       ├── ControlServer.kt              # HTTP server
│   │       ├── ControlServerService.kt       # Application service
│   │       ├── ControlServerApplicationListener.kt
│   │       ├── config/
│   │       │   ├── ServerConfig.kt
│   │       │   └── ConfigLoader.kt
│   │       ├── server/
│   │       │   ├── ResponseBuilder.kt
│   │       │   └── handlers/
│   │       │       └── HealthHandler.kt
│   │       └── settings/
│   │           ├── ControlServerConfigurable.kt    # Settings UI
│   │           └── ControlServerSettingsComponent.kt
│   └── resources/
│       └── META-INF/
│           └── plugin.xml                    # Plugin descriptor
└── test/
    └── kotlin/
        └── io/miiken/intellijcontrolserver/
            ├── config/
            │   ├── ServerConfigTest.kt
            │   └── ConfigLoaderTest.kt
            └── server/
                ├── ResponseBuilderTest.kt
                └── handlers/
                    └── HealthHandlerTest.kt
```

## Next Steps

- See `.cursor/tasks/` for development phases
- Check `API-SPEC.md` for endpoint documentation
- Review `ARCHITECTURE.md` for design decisions

## Useful Links

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Gradle IntelliJ Plugin](https://github.com/JetBrains/gradle-intellij-plugin)
- [Plugin Development Guidelines](https://plugins.jetbrains.com/docs/intellij/intellij-coding-guidelines.html)

