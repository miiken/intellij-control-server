package io.miiken.intellijcontrolserver.mcp

import com.google.gson.JsonObject
import io.miiken.intellijcontrolserver.ControlServerService

/**
 * MCP Tool: Health Check
 * 
 * Returns server health status and uptime
 */
class HealthCheckTool : McpTool {
    override val name = "intellij_health_check"
    
    override val description = "Check if the IntelliJ Control Server is running and get status information including uptime"
    
    override val inputSchema = mapOf(
        "type" to "object",
        "properties" to emptyMap<String, Any>(),
        "required" to emptyList<String>()
    )
    
    override fun execute(arguments: JsonObject): Any {
        val service = ControlServerService.getInstance()
        val server = service.server
        
        return if (server != null && server.isRunning()) {
            mapOf(
                "status" to "ok",
                "version" to "1.0.0",
                "uptime" to server.getUptime(),
                "timestamp" to System.currentTimeMillis()
            )
        } else {
            mapOf(
                "status" to "stopped",
                "message" to "Server is not running"
            )
        }
    }
}

