#!/bin/bash
set -e

# IntelliJ MCP Bridge Installer
# This script installs the MCP bridge to ~/.intellij-mcp-bridge/

VERSION="1.0.0"
INSTALL_DIR="$HOME/.intellij-mcp-bridge"
BRIDGE_DIR="$INSTALL_DIR/mcp-bridge-$VERSION"
DOWNLOAD_URL="https://github.com/miiken/intellij-control-server/releases/download/v$VERSION/mcp-bridge-$VERSION.zip"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  IntelliJ MCP Bridge Installer v$VERSION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if already installed
if [ -d "$BRIDGE_DIR" ]; then
    echo "⚠️  MCP Bridge v$VERSION is already installed at:"
    echo "   $BRIDGE_DIR"
    echo ""
    read -p "Do you want to reinstall? (y/N) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Installation cancelled."
        exit 0
    fi
    echo "Removing existing installation..."
    rm -rf "$BRIDGE_DIR"
fi

# Create install directory
echo "📁 Creating installation directory..."
mkdir -p "$INSTALL_DIR"

# Download
echo "⬇️  Downloading MCP Bridge v$VERSION..."
if command -v curl &> /dev/null; then
    curl -L "$DOWNLOAD_URL" -o "$INSTALL_DIR/mcp-bridge.zip"
elif command -v wget &> /dev/null; then
    wget "$DOWNLOAD_URL" -O "$INSTALL_DIR/mcp-bridge.zip"
else
    echo "❌ Error: Neither curl nor wget is installed."
    echo "   Please install curl or wget and try again."
    exit 1
fi

# Extract
echo "📦 Extracting..."
unzip -q "$INSTALL_DIR/mcp-bridge.zip" -d "$INSTALL_DIR"
rm "$INSTALL_DIR/mcp-bridge.zip"

# Make executable
echo "🔧 Setting permissions..."
chmod +x "$BRIDGE_DIR/bin/mcp-bridge"

# Test installation
echo "✅ Testing installation..."
if [ -x "$BRIDGE_DIR/bin/mcp-bridge" ]; then
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ✅ Installation Successful!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📍 Installed to: $BRIDGE_DIR"
    echo ""
    echo "📋 Next Steps:"
    echo ""
    echo "1. Add to your Cursor settings (⌘+Shift+J):"
    echo ""
    echo '   {'
    echo '     "mcpServers": {'
    echo '       "intellij": {'
    echo "         \"command\": \"$BRIDGE_DIR/bin/mcp-bridge\","
    echo '         "args": ["http://127.0.0.1:8767"]'
    echo '       }'
    echo '     }'
    echo '   }'
    echo ""
    echo "2. Restart Cursor"
    echo ""
    echo "3. Open a project in IntelliJ IDEA"
    echo ""
    echo "4. Ask Cursor to perform refactorings!"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "💡 Tip: You can test the bridge manually:"
    echo "   echo '{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"id\":1}' | $BRIDGE_DIR/bin/mcp-bridge http://localhost:8767"
    echo ""
else
    echo "❌ Installation failed. File is not executable."
    exit 1
fi

# Optional: Add to PATH
echo "❓ Add to PATH? (y/N)"
read -p "   This allows you to run 'mcp-bridge' from anywhere: " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    SHELL_RC="$HOME/.zshrc"
    if [ -f "$HOME/.bashrc" ]; then
        SHELL_RC="$HOME/.bashrc"
    fi
    
    if ! grep -q "intellij-mcp-bridge" "$SHELL_RC" 2>/dev/null; then
        echo "" >> "$SHELL_RC"
        echo "# IntelliJ MCP Bridge" >> "$SHELL_RC"
        echo "export PATH=\"$BRIDGE_DIR/bin:\$PATH\"" >> "$SHELL_RC"
        echo "✅ Added to $SHELL_RC"
        echo "   Run: source $SHELL_RC"
    else
        echo "ℹ️  Already in PATH"
    fi
fi

echo ""
echo "🎉 Happy coding!"

