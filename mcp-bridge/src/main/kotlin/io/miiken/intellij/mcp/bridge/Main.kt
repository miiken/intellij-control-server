package io.miiken.intellij.mcp.bridge

/**
 * MCP Bridge - Standalone executable
 * 
 * Translates MCP protocol (JSON-RPC over stdio) to HTTP calls to the IntelliJ plugin.
 * 
 * Usage:
 *   java -jar mcp-bridge.jar [http://127.0.0.1:8768]
 * 
 * The bridge is completely tool-agnostic - all tool definitions and logic
 * live in the plugin. The bridge just forwards protocol messages.
 */
fun main(args: Array<String>) {
    val httpBaseUrl = args.firstOrNull() ?: "http://127.0.0.1:8768"
    
    System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    System.err.println("  IntelliJ MCP Bridge v1.0.0")
    System.err.println("  Protocol: JSON-RPC 2.0 over stdio")
    System.err.println("  Plugin:   $httpBaseUrl")
    System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    System.err.println()
    
    val bridge = McpBridge(httpBaseUrl)
    bridge.start()
}

