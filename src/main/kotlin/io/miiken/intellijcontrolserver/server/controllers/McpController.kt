package io.miiken.intellijcontrolserver.server.controllers

import com.google.gson.Gson
import io.miiken.intellijcontrolserver.mcp.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

/**
 * Controller for MCP Bridge integration
 * 
 * Exposes the plugin's MCP ToolRegistry via HTTP for the standalone bridge
 */
@Path("/mcp")
@Tag(name = "MCP", description = "Model Context Protocol bridge endpoints")
class McpController {
    private val gson = Gson()
    
    @GET
    @Path("/tools")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "List MCP tools",
        description = "Returns all registered MCP tools with their schemas (used by MCP bridge)"
    )
    @ApiResponse(
        responseCode = "200",
        description = "List of available tools",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ToolsListResponse::class)
        )]
    )
    fun listTools(): ToolsListResponse {
        val tools = ToolRegistry.getAllTools().map { tool ->
            ToolDefinition(
                name = tool.name,
                description = tool.description,
                inputSchema = tool.inputSchema
            )
        }
        
        return ToolsListResponse(tools)
    }
    
    @POST
    @Path("/call")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Call MCP tool",
        description = "Executes an MCP tool with given arguments (used by MCP bridge)"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Tool execution result",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ToolCallResponse::class)
        )]
    )
    fun callTool(request: Map<String, Any>): ToolCallResponse {
        val name = request["name"] as? String
            ?: throw IllegalArgumentException("name is required")
        val arguments = request["arguments"] as? Map<String, Any> ?: emptyMap()
        
        val tool = ToolRegistry.getTool(name)
            ?: throw IllegalArgumentException("Unknown tool: $name")
        
        val result = executeTypedTool(tool, arguments)
        
        return ToolCallResponse(
            content = listOf(
                ContentItem(
                    type = "text",
                    text = gson.toJson(result)
                )
            )
        )
    }
    
    private fun <IN : Any, OUT : Any> executeTypedTool(
        tool: Tool<IN, OUT>,
        arguments: Map<String, Any>
    ): OUT {
        val argumentsJson = gson.toJson(arguments)
        val typedRequest = gson.fromJson(argumentsJson, tool.inputClass.java)
        return tool.execute(typedRequest)
    }
}

