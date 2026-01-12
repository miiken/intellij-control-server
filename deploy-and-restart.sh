#!/bin/bash
set -e

echo "🔨 Building plugin..."
./gradlew buildPlugin

echo ""
echo "📦 Installing plugin..."
PLUGIN_ZIP="build/distributions/intellij-control-server-1.0.0.zip"
PLUGINS_DIR="$HOME/Library/Application Support/JetBrains/IntelliJIdea2025.2/plugins"
INSTALL_DIR="$PLUGINS_DIR/intellij-control-server"

rm -rf "$INSTALL_DIR"
mkdir -p "$PLUGINS_DIR"
unzip -q "$PLUGIN_ZIP" -d "$PLUGINS_DIR"
echo "✅ Plugin installed to: $INSTALL_DIR"

echo ""
echo "🔄 Restarting IntelliJ..."

# Try to restart via MCP tool
if curl -s -X POST http://localhost:8767/intellij-control-server/system/restart > /dev/null 2>&1; then
    echo "✅ IntelliJ restart triggered successfully"
    echo ""
    echo "⏳ Waiting for IntelliJ to restart..."
    sleep 5
    
    # Wait for server to be back online
    for i in {1..30}; do
        if curl -s http://localhost:8767/health > /dev/null 2>&1; then
            echo "✅ IntelliJ is back online!"
            exit 0
        fi
        sleep 1
    done
    echo "⚠️  IntelliJ is taking longer than expected to restart"
    exit 1
else
    echo "⚠️  Could not trigger automatic restart via API"
    echo "Please restart IntelliJ manually"
    exit 1
fi
