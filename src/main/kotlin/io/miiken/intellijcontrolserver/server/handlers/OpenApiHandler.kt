package io.miiken.intellijcontrolserver.server.handlers

import com.intellij.openapi.diagnostic.Logger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.miiken.intellijcontrolserver.server.ResponseBuilder

class OpenApiHandler : HttpHandler {
    private val logger = Logger.getInstance(OpenApiHandler::class.java)
    private val openApiSpec: String by lazy {
        loadOpenApiSpec()
    }
    
    override fun handle(exchange: HttpExchange) {
        if (exchange.requestMethod != "GET") {
            ResponseBuilder.sendError(
                exchange,
                "METHOD_NOT_ALLOWED",
                "Only GET method is allowed for OpenAPI spec",
                statusCode = 405
            )
            return
        }
        
        try {
            exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            
            val bytes = openApiSpec.toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
            
            logger.debug("Served OpenAPI specification")
        } catch (e: Exception) {
            logger.error("Failed to serve OpenAPI spec", e)
            ResponseBuilder.sendError(
                exchange,
                "INTERNAL_ERROR",
                "Failed to serve OpenAPI specification",
                statusCode = 500
            )
        }
    }
    
    private fun loadOpenApiSpec(): String {
        return try {
            val resource = this::class.java.classLoader.getResource("openapi.json")
            if (resource == null) {
                logger.error("OpenAPI spec not found in resources")
                """{"error":"OpenAPI specification not found"}"""
            } else {
                resource.readText(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            logger.error("Failed to load OpenAPI spec", e)
            """{"error":"Failed to load OpenAPI specification: ${e.message}"}"""
        }
    }
}

