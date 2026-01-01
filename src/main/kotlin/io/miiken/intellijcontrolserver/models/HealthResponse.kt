package io.miiken.intellijcontrolserver.models

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Health check response")
data class HealthResponse(
    @Schema(description = "Server status", example = "ok")
    val status: String,
    
    @Schema(description = "Plugin version", example = "1.0.0")
    val version: String,
    
    @Schema(description = "Server uptime in seconds", example = "3600")
    val uptime: Long,
    
    @Schema(description = "Current timestamp in milliseconds", example = "1735725521000")
    val timestamp: Long
)

