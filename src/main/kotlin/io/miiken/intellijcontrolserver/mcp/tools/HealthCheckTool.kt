package io.miiken.intellijcontrolserver.mcp.tools

import io.miiken.intellijcontrolserver.ControlServerService
import io.miiken.intellijcontrolserver.mcp.Tool

/**
 * Health check tool for MCP
 * 
 * Checks if the IntelliJ Control Server is running and returns status information.
 */
class HealthCheckTool : Tool {
    override val name = "intellij_health_check"
    
    override val description = "Check if the IntelliJ Control Server is running and get status information including uptime"
    
    override val inputSchema = mapOf(
        "type" to "object",
        "properties" to emptyMap<String, Any>(),
        "required" to emptyList<String>()
    )
    
    override fun execute(arguments: Map<String, Any>): Any {
        val service = ControlServerService.getInstance()
        
        return if (service.isRunning()) {
            mapOf(
                "status" to "ok",
                "version" to "1.0.0",
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

