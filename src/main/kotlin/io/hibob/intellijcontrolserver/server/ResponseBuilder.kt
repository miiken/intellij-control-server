package io.hibob.intellijcontrolserver.server

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import java.io.OutputStream

/**
 * Utility for building HTTP responses
 */
object ResponseBuilder {
    private val gson = Gson()
    
    /**
     * Send a successful JSON response
     * 
     * @param exchange HTTP exchange
     * @param data Data to serialize as JSON
     * @param statusCode HTTP status code (default: 200)
     */
    fun sendSuccess(exchange: HttpExchange, data: Any, statusCode: Int = 200) {
        val response = mapOf("success" to true) + when (data) {
            is Map<*, *> -> data as Map<String, Any>
            else -> mapOf("data" to data)
        }
        sendJson(exchange, response, statusCode)
    }
    
    /**
     * Send an error JSON response
     * 
     * @param exchange HTTP exchange
     * @param code Error code
     * @param message Error message
     * @param details Optional error details
     * @param statusCode HTTP status code (default: 400)
     */
    fun sendError(
        exchange: HttpExchange,
        code: String,
        message: String,
        details: Map<String, Any>? = null,
        statusCode: Int = 400
    ) {
        val error = mutableMapOf(
            "code" to code,
            "message" to message
        )
        if (details != null) {
            error["details"] = details
        }
        
        val response = mapOf(
            "success" to false,
            "error" to error
        )
        sendJson(exchange, response, statusCode)
    }
    
    /**
     * Send a JSON response
     * 
     * @param exchange HTTP exchange
     * @param data Data to serialize
     * @param statusCode HTTP status code
     */
    fun sendJson(exchange: HttpExchange, data: Any, statusCode: Int = 200) {
        val json = gson.toJson(data)
        val bytes = json.toByteArray(Charsets.UTF_8)
        
        exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*") // Will be conditional based on config
        
        exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }
    
    /**
     * Send a text response
     * 
     * @param exchange HTTP exchange
     * @param text Text to send
     * @param statusCode HTTP status code
     */
    fun sendText(exchange: HttpExchange, text: String, statusCode: Int = 200) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        
        exchange.responseHeaders.set("Content-Type", "text/plain; charset=UTF-8")
        
        exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }
    
    /**
     * Parse JSON request body
     * 
     * @param exchange HTTP exchange
     * @param clazz Class to deserialize into
     * @return Deserialized object
     */
    fun <T> parseJson(exchange: HttpExchange, clazz: Class<T>): T {
        val body = exchange.requestBody.bufferedReader().use { it.readText() }
        return gson.fromJson(body, clazz)
    }
    
    /**
     * Parse JSON request body as Map
     * 
     * @param exchange HTTP exchange
     * @return Map of request data
     */
    fun parseJsonMap(exchange: HttpExchange): Map<String, Any> {
        val body = exchange.requestBody.bufferedReader().use { it.readText() }
        return gson.fromJson(body, Map::class.java) as Map<String, Any>
    }
    
    /**
     * Set CORS headers if enabled
     * 
     * @param exchange HTTP exchange
     * @param enableCors Whether CORS is enabled
     */
    fun setCorsHeaders(exchange: HttpExchange, enableCors: Boolean) {
        if (enableCors) {
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            exchange.responseHeaders.set("Access-Control-Allow-Headers", "Content-Type")
        }
    }
}

