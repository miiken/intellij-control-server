package io.miiken.intellijcontrolserver.server.handlers

import com.intellij.openapi.diagnostic.Logger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class SwaggerUIHandler : HttpHandler {
    private val logger = Logger.getInstance(SwaggerUIHandler::class.java)
    private val swaggerHtml: String by lazy {
        loadSwaggerUI()
    }
    
    override fun handle(exchange: HttpExchange) {
        if (exchange.requestMethod != "GET") {
            val response = "Only GET method is allowed"
            exchange.sendResponseHeaders(405, response.length.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
            return
        }
        
        try {
            exchange.responseHeaders.set("Content-Type", "text/html; charset=UTF-8")
            
            val bytes = swaggerHtml.toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
            
            logger.debug("Served Swagger UI")
        } catch (e: Exception) {
            logger.error("Failed to serve Swagger UI", e)
            val errorMsg = "Failed to load Swagger UI"
            exchange.sendResponseHeaders(500, errorMsg.length.toLong())
            exchange.responseBody.use { it.write(errorMsg.toByteArray()) }
        }
    }
    
    private fun loadSwaggerUI(): String {
        return try {
            val resource = this::class.java.classLoader.getResource("swagger-ui.html")
            if (resource == null) {
                logger.error("Swagger UI HTML not found in resources")
                "<html><body><h1>Swagger UI not found</h1></body></html>"
            } else {
                resource.readText(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            logger.error("Failed to load Swagger UI HTML", e)
            "<html><body><h1>Failed to load Swagger UI: ${e.message}</h1></body></html>"
        }
    }
}

