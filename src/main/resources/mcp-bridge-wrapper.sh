#!/bin/bash
# IntelliJ MCP Bridge Wrapper
# This script is auto-installed by the IntelliJ Control Server plugin
# It finds the plugin installation and runs the embedded MCP bridge

set -e

# Find the IntelliJ plugin directory
PLUGIN_DIR=""

# Check common plugin base directories (without globbing first)
JETBRAINS_DIRS=(
    "$HOME/Library/Application Support/JetBrains"
    "$HOME/.local/share/JetBrains"
    "$HOME/.config/JetBrains"
)

for base_dir in "${JETBRAINS_DIRS[@]}"; do
    if [ -d "$base_dir" ]; then
        # Now glob within the found directory
        for idea_version in "$base_dir"/IntelliJIdea* "$base_dir"/IdeaIC*; do
            if [ -d "$idea_version" ]; then
                plugin_path="$idea_version/plugins/intellij-control-server"
                if [ -d "$plugin_path" ] && [ -f "$plugin_path/lib/mcp-bridge.jar" ]; then
                    PLUGIN_DIR="$plugin_path"
                    break 2
                fi
            fi
        done
    fi
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

