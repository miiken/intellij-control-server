#!/bin/bash
# IntelliJ MCP Bridge Wrapper
# This script is auto-installed by the IntelliJ Control Server plugin
# It finds the plugin installation and runs the embedded MCP bridge

set -e

# Find the IntelliJ plugin directory
PLUGIN_DIR=""

# Check common plugin locations
PLUGIN_LOCATIONS=(
    "$HOME/Library/Application Support/JetBrains/IntelliJIdea*/plugins/intellij-control-server"
    "$HOME/.local/share/JetBrains/IntelliJIdea*/plugins/intellij-control-server"
    "$HOME/.config/JetBrains/IntelliJIdea*/plugins/intellij-control-server"
)

for pattern in "${PLUGIN_LOCATIONS[@]}"; do
    for dir in $pattern; do
        if [ -d "$dir" ] && [ -f "$dir/lib/mcp-bridge.jar" ]; then
            PLUGIN_DIR="$dir"
            break 2
        fi
    done
done

if [ -z "$PLUGIN_DIR" ]; then
    echo "Error: IntelliJ Control Server plugin not found" >&2
    echo "Please ensure the plugin is installed and IntelliJ has been run at least once" >&2
    exit 1
fi

# Find Java (use IntelliJ's bundled JRE if available)
JAVA_CMD="java"
if [ -d "/Applications/IntelliJ IDEA.app" ]; then
    IDEA_JAVA="/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home/bin/java"
    if [ -x "$IDEA_JAVA" ]; then
        JAVA_CMD="$IDEA_JAVA"
    fi
fi

# Get port from config or use default
PORT="${INTELLIJ_MCP_PORT:-8767}"

# Run the bridge
exec "$JAVA_CMD" -jar "$PLUGIN_DIR/lib/mcp-bridge.jar" "http://127.0.0.1:$PORT" "$@"

