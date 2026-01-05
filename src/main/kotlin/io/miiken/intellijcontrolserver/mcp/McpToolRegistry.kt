package io.miiken.intellijcontrolserver.mcp

import com.google.gson.JsonObject

/**
 * Registry of MCP tools
 * 
 * Manages available tools and their execution
 */
class McpToolRegistry {
    private val tools = mutableMapOf<String, McpTool>()
    
    init {
        registerDefaultTools()
    }
    
    fun registerTool(tool: McpTool) {
        tools[tool.name] = tool
    }
    
    fun getTool(name: String): McpTool? = tools[name]
    
    fun getAllTools(): List<McpTool> = tools.values.toList()
    
    fun executeTool(name: String, arguments: JsonObject): Any {
        val tool = tools[name] ?: throw IllegalArgumentException("Tool not found: $name")
        return tool.execute(arguments)
    }
    
    private fun registerDefaultTools() {
        registerTool(HealthCheckTool())
        registerTool(RenameSymbolTool())
    }
}

/**
 * Base interface for MCP tools
 */
interface McpTool {
    val name: String
    val description: String
    val inputSchema: Map<String, Any>
    
    fun execute(arguments: JsonObject): Any
}

