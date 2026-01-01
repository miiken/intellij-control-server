package io.miiken.intellijcontrolserver.server.handlers

import com.intellij.openapi.diagnostic.Logger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.miiken.intellijcontrolserver.server.ResponseBuilder

/**
 * Handler for health check endpoint
 * 
 * GET /health
 * Returns server status, version, and uptime
 */
class HealthHandler(private val startTime: Long) : HttpHandler {
    private val logger = Logger.getInstance(HealthHandler::class.java)
    
    override fun handle(exchange: HttpExchange) {
        try {
            logger.debug("Handling health check request")
            
            if (exchange.requestMethod != "GET") {
                ResponseBuilder.sendError(
                    exchange,
                    "METHOD_NOT_ALLOWED",
                    "Only GET method is allowed for health check",
                    statusCode = 405
                )
                return
            }
            
            val uptime = System.currentTimeMillis() - startTime
            val uptimeSeconds = uptime / 1000
            
            val response = mapOf(
                "status" to "ok",
                "version" to "1.0.0",
                "uptime" to uptimeSeconds,
                "timestamp" to System.currentTimeMillis()
            )
            
            ResponseBuilder.sendSuccess(exchange, response)
            logger.debug("Health check successful")
            
        } catch (e: Exception) {
            logger.error("Error handling health check request", e)
            ResponseBuilder.sendError(
                exchange,
                "SERVER_ERROR",
                "Internal server error: ${e.message}",
                statusCode = 500
            )
        }
    }
}

