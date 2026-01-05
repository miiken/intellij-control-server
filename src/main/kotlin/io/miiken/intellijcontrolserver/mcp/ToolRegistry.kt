package io.miiken.intellijcontrolserver.mcp

import com.intellij.openapi.diagnostic.Logger
import io.miiken.intellijcontrolserver.mcp.tools.ExtractMethodTool
import io.miiken.intellijcontrolserver.mcp.tools.HealthCheckTool
import io.miiken.intellijcontrolserver.mcp.tools.RenameSymbolTool

/**
 * Registry for MCP tools
 * 
 * Auto-discovers and manages all available tools.
 * To add a new tool: create a class implementing Tool interface in the tools package.
 */
object ToolRegistry {
    private val logger = Logger.getInstance(ToolRegistry::class.java)
    private val tools: Map<String, Tool>
    
    init {
        tools = discoverTools()
        logger.info("Registered ${tools.size} MCP tools: ${tools.keys}")
    }
    
    fun getAllTools(): List<Tool> = tools.values.toList()
    
    fun getTool(name: String): Tool? = tools[name]
    
    private fun discoverTools(): Map<String, Tool> {
        val toolInstances = listOf(
            HealthCheckTool(),
            RenameSymbolTool(),
            ExtractMethodTool()
        )
        
        return toolInstances.associateBy { it.name }
    }
}

