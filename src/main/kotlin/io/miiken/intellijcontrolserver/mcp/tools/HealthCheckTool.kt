package io.miiken.intellijcontrolserver.mcp.tools

import io.miiken.intellijcontrolserver.ControlServerService
import io.miiken.intellijcontrolserver.mcp.Tool
import io.miiken.intellijcontrolserver.mcp.ToolRegistry
import io.miiken.intellijcontrolserver.mcp.models.EmptyRequest
import io.miiken.intellijcontrolserver.models.HealthResponse
import kotlin.reflect.KClass

/**
 * Health check tool for MCP
 * 
 * Checks if the IntelliJ Control Server is running and returns status information.
 */
class HealthCheckTool private constructor() : Tool<EmptyRequest, HealthResponse> {
    override val name = "intellij_health_check"
    
    override val description = "Check if the IntelliJ Control Server is running and get status information"
    
    override val inputSchema = mapOf(
        "type" to "object",
        "properties" to emptyMap<String, Any>(),
        "required" to emptyList<String>()
    )
    
    override val inputClass: KClass<EmptyRequest> = EmptyRequest::class
    
    override fun execute(request: EmptyRequest): HealthResponse {
        val service = ControlServerService.getInstance()
        
        return if (service.isRunning()) {
            HealthResponse(
                status = "ok",
                version = "1.0.0",
                uptime = 0,
                timestamp = System.currentTimeMillis()
            )
        } else {
            HealthResponse(
                status = "stopped",
                version = "1.0.0",
                uptime = 0,
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    companion object {
        init {
            ToolRegistry.register(HealthCheckTool())
        }
    }
}

