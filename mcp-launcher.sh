#!/bin/bash
# MCP Server Launcher for Cursor
#
# This script connects to the MCP server running in the IntelliJ plugin
# via a simple proxy that forwards stdio to the plugin's MCP interface.

# For now, we'll echo a message explaining the setup
# In a production version, this would be a proper stdio proxy

cat << 'EOF' >&2
ERROR: MCP Server requires IntelliJ IDEA to be running with the plugin installed.

Current limitation: The MCP server is integrated into the IntelliJ plugin
and runs within the IDE process. Cursor cannot directly connect to it yet.

Workaround options:
1. Use the HTTP REST API instead (already working at http://127.0.0.1:8768)
2. Implement a standalone MCP server wrapper (future enhancement)

Available HTTP endpoints:
  - GET  /health
  - POST /{projectName}/refactor/rename
  - POST /{projectName}/refactor/extract-method

For API documentation: http://127.0.0.1:8768/api-docs
EOF

exit 1

