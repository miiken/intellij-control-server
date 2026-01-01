package io.miiken.intellijcontrolserver.server

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResponseBuilderTest {
    
    private val gson = Gson()
    
    @Test
    fun `should send success response with data`() {
        val exchange = createMockExchange()
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        val data = mapOf("message" to "Hello")
        ResponseBuilder.sendSuccess(exchange, data)
        
        val response = gson.fromJson(outputStream.toString(), Map::class.java)
        assertEquals(true, response["success"])
        assertEquals("Hello", response["message"])
    }
    
    @Test
    fun `should send error response with code and message`() {
        val exchange = createMockExchange()
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        ResponseBuilder.sendError(exchange, "TEST_ERROR", "Test error message")
        
        val response = gson.fromJson(outputStream.toString(), Map::class.java)
        assertEquals(false, response["success"])
        val error = response["error"] as Map<*, *>
        assertEquals("TEST_ERROR", error["code"])
        assertEquals("Test error message", error["message"])
    }
    
    @Test
    fun `should send error response with details`() {
        val exchange = createMockExchange()
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        val details = mapOf("field" to "port", "value" to "invalid")
        ResponseBuilder.sendError(exchange, "VALIDATION_ERROR", "Invalid field", details)
        
        val response = gson.fromJson(outputStream.toString(), Map::class.java)
        val error = response["error"] as Map<*, *>
        val responseDetails = error["details"] as Map<*, *>
        assertEquals("port", responseDetails["field"])
        assertEquals("invalid", responseDetails["value"])
    }
    
    @Test
    fun `should set JSON content type header`() {
        val exchange = createMockExchange()
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        ResponseBuilder.sendJson(exchange, mapOf("test" to "data"))
        
        verify { exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8") }
    }
    
    @Test
    fun `should send text response`() {
        val exchange = createMockExchange()
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        ResponseBuilder.sendText(exchange, "Plain text")
        
        assertEquals("Plain text", outputStream.toString())
        verify { exchange.responseHeaders.set("Content-Type", "text/plain; charset=UTF-8") }
    }
    
    @Test
    fun `should send response with custom status code`() {
        val exchange = createMockExchange()
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        ResponseBuilder.sendSuccess(exchange, mapOf("test" to "data"), statusCode = 201)
        
        verify { exchange.sendResponseHeaders(201, any()) }
    }
    
    @Test
    fun `should send error with 400 status by default`() {
        val exchange = createMockExchange()
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        ResponseBuilder.sendError(exchange, "ERROR", "Message")
        
        verify { exchange.sendResponseHeaders(400, any()) }
    }
    
    @Test
    fun `should set CORS headers when enabled`() {
        val exchange = createMockExchange()
        
        ResponseBuilder.setCorsHeaders(exchange, enableCors = true)
        
        verify { exchange.responseHeaders.set("Access-Control-Allow-Origin", "*") }
        verify { exchange.responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS") }
        verify { exchange.responseHeaders.set("Access-Control-Allow-Headers", "Content-Type") }
    }
    
    @Test
    fun `should not set CORS headers when disabled`() {
        val exchange = createMockExchange()
        
        ResponseBuilder.setCorsHeaders(exchange, enableCors = false)
        
        verify(exactly = 0) { exchange.responseHeaders.set("Access-Control-Allow-Origin", any()) }
    }
    
    private fun createMockExchange(): HttpExchange {
        val exchange = mockk<HttpExchange>(relaxed = true)
        every { exchange.responseHeaders } returns mockk(relaxed = true)
        return exchange
    }
}

