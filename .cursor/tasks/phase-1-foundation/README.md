# Phase 1: Foundation

**Timeline**: Week 1  
**Status**: Not Started  
**Goal**: Basic infrastructure and health check

## Overview

This phase establishes the core infrastructure for the IntelliJ Control Server plugin. We'll create the HTTP server, plugin lifecycle management, configuration system, and basic health check endpoint.

## Objectives

1. **HTTP Server**: Embed Sun HttpServer in IntelliJ plugin
2. **Health Check**: Implement `/health` endpoint
3. **Configuration**: Load config from file or use defaults
4. **Logging**: Set up structured logging
5. **Error Handling**: Basic error response framework

## Deliverables

- [ ] Plugin starts HTTP server on IntelliJ startup
- [ ] Server binds to `127.0.0.1:8765` (configurable)
- [ ] `/health` endpoint returns status JSON
- [ ] Configuration loaded from `~/.intellij-control-server/config.json`
- [ ] Logs written to IntelliJ log file
- [ ] Server stops gracefully on plugin disposal

## Success Criteria

```bash
# Start IntelliJ with plugin installed
# Run health check
curl http://localhost:8765/health

# Expected response:
{
  "status": "ok",
  "version": "1.0.0",
  "uptime": 42
}
```

## Key Files to Create

- `src/main/kotlin/io/hibob/intellijcontrolserver/ControlServer.kt`
- `src/main/kotlin/io/hibob/intellijcontrolserver/server/HttpServer.kt`
- `src/main/kotlin/io/hibob/intellijcontrolserver/server/handlers/HealthHandler.kt`
- `src/main/kotlin/io/hibob/intellijcontrolserver/config/ServerConfig.kt`
- `src/main/resources/META-INF/plugin.xml`

## Dependencies

- IntelliJ Platform SDK (2023.3+)
- Kotlin stdlib
- Gson (for JSON serialization)

## Next Phase

→ **Phase 2**: Tasks API implementation

