package io.miiken.intellijcontrolserver.mcp

/**
 * Base interface for MCP tools
 * 
 * Implement this interface to create a new tool.
 * The tool will be automatically discovered and registered.
 */
interface Tool {
    /**
     * Unique tool name (e.g., "intellij_health_check")
     */
    val name: String
    
    /**
     * Human-readable description
     */
    val description: String
    
    /**
     * JSON Schema for input validation
     */
    val inputSchema: Map<String, Any>
    
    /**
     * Execute the tool with given arguments
     * 
     * @param arguments Tool arguments from MCP client
     * @return Tool result (will be JSON-serialized)
     * @throws IllegalArgumentException for invalid arguments
     * @throws RuntimeException for execution errors
     */
    fun execute(arguments: Map<String, Any>): Any
}

