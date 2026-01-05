package io.miiken.intellijcontrolserver.mcp

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

/**
 * Implementation of MCP Service
 * 
 * Handles MCP protocol methods and delegates to tool registry
 */
class McpServiceImpl : McpService {
    private val logger = Logger.getInstance(McpServiceImpl::class.java)
    private val gson = Gson()
    
    override fun initialize(params: Map<String, Any>?): InitializeResponse {
        logger.info("MCP initialize called")
        return InitializeResponse(
            protocolVersion = "2024-11-05",
            serverInfo = ServerInfo(
                name = "intellij-control-server",
                version = "1.0.0"
            ),
            capabilities = Capabilities(
                tools = emptyMap()
            )
        )
    }
    
    override fun listTools(): ToolsListResponse {
        logger.info("MCP tools/list called")
        
        val tools = ToolRegistry.getAllTools().map { tool ->
            ToolDefinition(
                name = tool.name,
                description = tool.description,
                inputSchema = tool.inputSchema
            )
        }
        
        return ToolsListResponse(tools)
    }
    
    override fun callTool(name: String, arguments: Map<String, Any>?): ToolCallResponse {
        logger.info("MCP tools/call: $name")
        
        val tool = ToolRegistry.getTool(name)
            ?: throw IllegalArgumentException("Unknown tool: $name")
        
        val result = tool.execute(arguments ?: emptyMap())
        
        return ToolCallResponse(
            content = listOf(
                ContentItem(
                    type = "text",
                    text = gson.toJson(result)
                )
            )
        )
    }
}

