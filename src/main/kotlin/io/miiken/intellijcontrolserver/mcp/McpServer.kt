package io.miiken.intellijcontrolserver.mcp

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * MCP (Model Context Protocol) Server
 * 
 * Implements JSON-RPC 2.0 over stdio for communication with AI clients like Cursor.
 * Runs alongside the HTTP REST API to provide native tool integration.
 */
class McpServer {
    private val logger = Logger.getInstance(McpServer::class.java)
    private val gson = Gson()
    private val running = AtomicBoolean(false)
    private val toolRegistry = McpToolRegistry()
    
    fun start() {
        if (running.getAndSet(true)) {
            logger.warn("MCP server already running")
            return
        }
        
        logger.info("Starting MCP server on stdio")
        
        Thread {
            try {
                runServerLoop()
            } catch (e: Exception) {
                logger.error("MCP server error", e)
            } finally {
                running.set(false)
            }
        }.start()
        
        logger.info("MCP server started")
    }
    
    fun stop() {
        if (!running.getAndSet(false)) {
            logger.warn("MCP server not running")
            return
        }
        logger.info("MCP server stopped")
    }
    
    fun isRunning(): Boolean = running.get()
    
    private fun runServerLoop() {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        val writer = PrintWriter(OutputStreamWriter(System.out), true)
        
        while (running.get()) {
            try {
                val line = reader.readLine() ?: break
                if (line.isBlank()) continue
                
                logger.debug("MCP received: $line")
                
                val request = gson.fromJson(line, JsonObject::class.java)
                val response = handleRequest(request)
                
                val responseJson = gson.toJson(response)
                logger.debug("MCP sending: $responseJson")
                
                writer.println(responseJson)
                writer.flush()
                
            } catch (e: Exception) {
                logger.error("Error processing MCP request", e)
                val error = createErrorResponse(null, -32603, "Internal error: ${e.message}")
                writer.println(gson.toJson(error))
                writer.flush()
            }
        }
    }
    
    private fun handleRequest(request: JsonObject): JsonObject {
        val method = request.get("method")?.asString
        val params = request.get("params")?.asJsonObject
        val id = request.get("id")
        
        return when (method) {
            "initialize" -> handleInitialize(id)
            "tools/list" -> handleToolsList(id)
            "tools/call" -> handleToolCall(id, params)
            else -> createErrorResponse(id, -32601, "Method not found: $method")
        }
    }
    
    private fun handleInitialize(id: com.google.gson.JsonElement?): JsonObject {
        return createSuccessResponse(id, mapOf(
            "protocolVersion" to "2024-11-05",
            "serverInfo" to mapOf(
                "name" to "intellij-control-server",
                "version" to "1.0.0"
            ),
            "capabilities" to mapOf(
                "tools" to emptyMap<String, Any>()
            )
        ))
    }
    
    private fun handleToolsList(id: com.google.gson.JsonElement?): JsonObject {
        val tools = toolRegistry.getAllTools().map { tool ->
            mapOf(
                "name" to tool.name,
                "description" to tool.description,
                "inputSchema" to tool.inputSchema
            )
        }
        
        return createSuccessResponse(id, mapOf("tools" to tools))
    }
    
    private fun handleToolCall(id: com.google.gson.JsonElement?, params: JsonObject?): JsonObject {
        if (params == null) {
            return createErrorResponse(id, -32602, "Missing params")
        }
        
        val toolName = params.get("name")?.asString
        if (toolName == null) {
            return createErrorResponse(id, -32602, "Missing tool name")
        }
        
        val arguments = params.get("arguments")?.asJsonObject ?: JsonObject()
        
        return try {
            val result = toolRegistry.executeTool(toolName, arguments)
            createSuccessResponse(id, mapOf(
                "content" to listOf(
                    mapOf(
                        "type" to "text",
                        "text" to gson.toJson(result)
                    )
                )
            ))
        } catch (e: Exception) {
            logger.error("Tool execution error", e)
            createErrorResponse(id, -32000, "Tool execution failed: ${e.message}")
        }
    }
    
    private fun createSuccessResponse(id: com.google.gson.JsonElement?, result: Any): JsonObject {
        return JsonObject().apply {
            addProperty("jsonrpc", "2.0")
            add("result", gson.toJsonTree(result))
            if (id != null) add("id", id)
        }
    }
    
    private fun createErrorResponse(id: com.google.gson.JsonElement?, code: Int, message: String): JsonObject {
        return JsonObject().apply {
            addProperty("jsonrpc", "2.0")
            add("error", JsonObject().apply {
                addProperty("code", code)
                addProperty("message", message)
            })
            if (id != null) add("id", id)
        }
    }
}

