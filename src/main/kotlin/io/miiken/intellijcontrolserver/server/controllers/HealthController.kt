package io.miiken.intellijcontrolserver.server.controllers

import io.miiken.intellijcontrolserver.models.HealthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/")
@Tag(name = "Health", description = "Server health and status operations")
class HealthController(private val startTime: Long) {
    
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Health check",
        description = "Check if the server is running and get uptime information"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Server is healthy",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = HealthResponse::class),
            examples = [ExampleObject(
                value = """{"status":"ok","version":"1.0.0","uptime":3600,"timestamp":1735725521000}"""
            )]
        )]
    )
    fun health(): HealthResponse {
        val uptime = (System.currentTimeMillis() - startTime) / 1000
        
        return HealthResponse(
            status = "ok",
            version = "1.0.0",
            uptime = uptime,
            timestamp = System.currentTimeMillis()
        )
    }
}

