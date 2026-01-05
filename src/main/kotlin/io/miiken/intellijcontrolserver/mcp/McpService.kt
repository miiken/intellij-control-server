package io.miiken.intellijcontrolserver.mcp

import com.googlecode.jsonrpc4j.JsonRpcMethod
import com.googlecode.jsonrpc4j.JsonRpcParam

/**
 * MCP (Model Context Protocol) Service Interface
 * 
 * Defines the JSON-RPC methods for the MCP server.
 * Uses jsonrpc4j annotations for automatic request/response handling.
 */
interface McpService {
    
    /**
     * Initialize the MCP session
     * 
     * @param params Initialization parameters
     * @return Server capabilities and information
     */
    @JsonRpcMethod("initialize")
    fun initialize(@JsonRpcParam("params") params: Map<String, Any>?): InitializeResponse
    
    /**
     * List available tools
     * 
     * @return List of tools with their schemas
     */
    @JsonRpcMethod("tools/list")
    fun listTools(): ToolsListResponse
    
    /**
     * Call a tool with parameters
     * 
     * @param name Tool name
     * @param arguments Tool arguments
     * @return Tool execution result
     */
    @JsonRpcMethod("tools/call")
    fun callTool(
        @JsonRpcParam("name") name: String,
        @JsonRpcParam("arguments") arguments: Map<String, Any>?
    ): ToolCallResponse
}

/**
 * Response for initialize method
 */
data class InitializeResponse(
    val protocolVersion: String,
    val serverInfo: ServerInfo,
    val capabilities: Capabilities
)

data class ServerInfo(
    val name: String,
    val version: String
)

data class Capabilities(
    val tools: Map<String, Any> = emptyMap()
)

/**
 * Response for tools/list method
 */
data class ToolsListResponse(
    val tools: List<ToolDefinition>
)

data class ToolDefinition(
    val name: String,
    val description: String,
    val inputSchema: Map<String, Any>
)

/**
 * Response for tools/call method
 */
data class ToolCallResponse(
    val content: List<ContentItem>
)

data class ContentItem(
    val type: String = "text",
    val text: String
)

