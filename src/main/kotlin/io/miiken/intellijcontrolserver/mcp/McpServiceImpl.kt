package io.miiken.intellijcontrolserver.mcp

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

/**
 * Implementation of MCP Service
 * 
 * Handles MCP protocol methods and delegates to tool registry.
 * Uses Gson for type-safe deserialization of requests.
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
        logger.info("MCP tools/list called (${ToolRegistry.getToolCount()} tools available)")
        
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
        
        val result = executeTypedTool(tool, arguments ?: emptyMap())
        
        return ToolCallResponse(
            content = listOf(
                ContentItem(
                    type = "text",
                    text = gson.toJson(result)
                )
            )
        )
    }
    
    private fun <IN : Any, OUT : Any> executeTypedTool(tool: Tool<IN, OUT>, arguments: Map<String, Any>): OUT {
        val argumentsJson = gson.toJson(arguments)
        val typedRequest = gson.fromJson(argumentsJson, tool.inputClass.java)
        return tool.execute(typedRequest)
    }
    
}

