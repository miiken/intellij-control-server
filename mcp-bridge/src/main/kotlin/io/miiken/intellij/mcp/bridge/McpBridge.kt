package io.miiken.intellij.mcp.bridge

import com.google.gson.Gson
import com.googlecode.jsonrpc4j.JsonRpcBasicServer
import com.googlecode.jsonrpc4j.JsonRpcMethod
import com.googlecode.jsonrpc4j.JsonRpcParam
import java.io.*

/**
 * MCP Bridge Service
 * 
 * Implements MCP protocol and delegates to BridgeToolRegistry
 */
interface McpBridgeService {
    @JsonRpcMethod("initialize")
    fun initialize(
        @JsonRpcParam("protocolVersion") protocolVersion: String?,
        @JsonRpcParam("capabilities") capabilities: Map<String, Any>?,
        @JsonRpcParam("clientInfo") clientInfo: Map<String, Any>?
    ): InitializeResponse
    
    @JsonRpcMethod("tools/list")
    fun listTools(): ToolsListResponse
    
    @JsonRpcMethod("tools/call")
    fun callTool(
        @JsonRpcParam("name") name: String,
        @JsonRpcParam("arguments") arguments: Map<String, Any>?
    ): ToolCallResponse
}

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

data class ToolsListResponse(
    val tools: List<ToolDefinition>
)

data class ToolDefinition(
    val name: String,
    val description: String,
    val inputSchema: Map<String, Any>
)

data class ToolCallResponse(
    val content: List<ContentItem>
)

data class ContentItem(
    val type: String = "text",
    val text: String
)

/**
 * Implementation of MCP Bridge Service
 */
class McpBridgeServiceImpl(private val httpBaseUrl: String) : McpBridgeService {
    private val gson = Gson()
    
    override fun initialize(
        protocolVersion: String?,
        capabilities: Map<String, Any>?,
        clientInfo: Map<String, Any>?
    ): InitializeResponse {
        System.err.println("INFO: MCP initialize called (client protocol: $protocolVersion)")
        // Support multiple protocol versions
        val responseVersion = protocolVersion ?: "2024-11-05"
        return InitializeResponse(
            protocolVersion = responseVersion,
            serverInfo = ServerInfo(
                name = "intellij-mcp-bridge",
                version = "1.0.0"
            ),
            capabilities = Capabilities(
                tools = emptyMap()
            )
        )
    }
    
    override fun listTools(): ToolsListResponse {
        System.err.println("INFO: MCP tools/list - forwarding to plugin")
        
        // Forward to plugin's MCP endpoint
        val response = PluginHttpClient.get("$httpBaseUrl/mcp/tools", ToolsListResponse::class)
        
        System.err.println("INFO: Received ${response.tools.size} tools from plugin")
        return response
    }
    
    override fun callTool(name: String, arguments: Map<String, Any>?): ToolCallResponse {
        System.err.println("INFO: MCP tools/call: $name - forwarding to plugin")
        
        // Forward to plugin's MCP endpoint
        val requestBody = mapOf(
            "name" to name,
            "arguments" to (arguments ?: emptyMap<String, Any>())
        )
        
        val response = PluginHttpClient.post("$httpBaseUrl/mcp/call", requestBody, ToolCallResponse::class)
        
        System.err.println("INFO: Tool execution completed")
        return response
    }
}

/**
 * MCP Bridge - stdio server
 */
class McpBridge(private val httpBaseUrl: String) {
    private val service = McpBridgeServiceImpl(httpBaseUrl)
    private val jsonRpcServer = JsonRpcBasicServer(service, McpBridgeService::class.java)
    
    fun start() {
        System.err.println("INFO: Starting MCP Bridge (stdio)")
        System.err.println("INFO: Forwarding to plugin at $httpBaseUrl")
        System.err.println("INFO: Bridge is tool-agnostic - all tools loaded from plugin")
        
        BufferedReader(InputStreamReader(System.`in`, Charsets.UTF_8)).use { reader ->
            PrintWriter(OutputStreamWriter(System.out, Charsets.UTF_8), true).use { writer ->
                
                while (true) {
                    try {
                        val line = reader.readLine() ?: break
                        
                        if (line.isBlank()) {
                            continue
                        }
                        
                        System.err.println("DEBUG: Received: $line")
                        
                        val response = processRequest(line)
                        
                        System.err.println("DEBUG: Sending: $response")
                        
                        writer.println(response)
                        writer.flush()
                        
                    } catch (e: Exception) {
                        System.err.println("ERROR: Request processing error: ${e.message}")
                        e.printStackTrace(System.err)
                        
                        val errorResponse = """{"jsonrpc":"2.0","error":{"code":-32603,"message":"Internal error: ${e.message}"},"id":null}"""
                        writer.println(errorResponse)
                        writer.flush()
                    }
                }
            }
        }
        
        System.err.println("INFO: MCP Bridge stopped")
    }
    
    private fun processRequest(requestJson: String): String {
        val requestBytes = requestJson.toByteArray(Charsets.UTF_8)
        val inputStream = ByteArrayInputStream(requestBytes)
        val outputStream = ByteArrayOutputStream()
        
        jsonRpcServer.handleRequest(inputStream, outputStream)
        
        return String(outputStream.toByteArray(), Charsets.UTF_8).trim()
    }
}

