package io.miiken.intellijcontrolserver.server.controllers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

/**
 * System operations controller for IDE management.
 */
@Path("system")
@Tag(name = "System", description = "IDE system operations (restart, etc.)")
class SystemController {
    
    @POST
    @Path("/restart")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Restart IntelliJ IDE",
        description = "Triggers a restart of the IntelliJ IDE. Useful after plugin updates. " +
                     "The server will be unavailable during restart (~10-30 seconds)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Restart initiated successfully"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Failed to initiate restart"
    )
    fun restart(): Map<String, Any> {
        try {
            // Schedule restart on EDT
            ApplicationManager.getApplication().invokeLater {
                val app = ApplicationManager.getApplication() as ApplicationEx
                app.restart(true)
            }
            
            return mapOf(
                "success" to true,
                "message" to "IDE restart initiated. Server will be unavailable for ~10-30 seconds.",
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            return mapOf(
                "success" to false,
                "message" to "Failed to restart IDE: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            )
        }
    }
    
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get IDE Status",
        description = "Returns basic IDE status information"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Status retrieved successfully"
    )
    fun status(): Map<String, Any> {
        val app = ApplicationManager.getApplication()
        return mapOf(
            "alive" to true,
            "version" to ApplicationManager.getApplication().javaClass.`package`.implementationVersion.orEmpty(),
            "isUnitTestMode" to app.isUnitTestMode,
            "isHeadless" to app.isHeadlessEnvironment,
            "timestamp" to System.currentTimeMillis()
        )
    }
}
