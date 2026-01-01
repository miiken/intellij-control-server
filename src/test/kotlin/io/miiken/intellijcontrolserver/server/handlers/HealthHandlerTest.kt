package io.miiken.intellijcontrolserver.server.handlers

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthHandlerTest {
    
    private val gson = Gson()
    private val startTime = System.currentTimeMillis()
    
    @Test
    fun `should return success for GET request`() {
        val handler = HealthHandler(startTime)
        val exchange = createMockExchange("GET")
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        handler.handle(exchange)
        
        val response = gson.fromJson(outputStream.toString(), Map::class.java)
        assertEquals(true, response["success"])
        assertEquals("ok", response["status"])
        assertEquals("1.0.0", response["version"])
        assertTrue(response.containsKey("uptime"))
        assertTrue(response.containsKey("timestamp"))
    }
    
    @Test
    fun `should return 405 for non-GET request`() {
        val handler = HealthHandler(startTime)
        val exchange = createMockExchange("POST")
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        handler.handle(exchange)
        
        verify { exchange.sendResponseHeaders(405, any()) }
        val response = gson.fromJson(outputStream.toString(), Map::class.java)
        assertEquals(false, response["success"])
        val error = response["error"] as Map<*, *>
        assertEquals("METHOD_NOT_ALLOWED", error["code"])
    }
    
    @Test
    fun `should calculate uptime in seconds`() {
        val testStartTime = System.currentTimeMillis() - 5000
        val handler = HealthHandler(testStartTime)
        val exchange = createMockExchange("GET")
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        handler.handle(exchange)
        
        val response = gson.fromJson(outputStream.toString(), Map::class.java)
        val uptime = (response["uptime"] as Double).toLong()
        assertTrue(uptime >= 5, "Uptime should be at least 5 seconds")
    }
    
    @Test
    fun `should include timestamp in response`() {
        val handler = HealthHandler(startTime)
        val exchange = createMockExchange("GET")
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        val beforeTime = System.currentTimeMillis()
        handler.handle(exchange)
        val afterTime = System.currentTimeMillis()
        
        val response = gson.fromJson(outputStream.toString(), Map::class.java)
        val timestamp = (response["timestamp"] as Double).toLong()
        assertTrue(timestamp >= beforeTime && timestamp <= afterTime)
    }
    
    @Test
    fun `should return 405 for PUT request`() {
        val handler = HealthHandler(startTime)
        val exchange = createMockExchange("PUT")
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        handler.handle(exchange)
        
        verify { exchange.sendResponseHeaders(405, any()) }
    }
    
    @Test
    fun `should return 405 for DELETE request`() {
        val handler = HealthHandler(startTime)
        val exchange = createMockExchange("DELETE")
        val outputStream = ByteArrayOutputStream()
        every { exchange.responseBody } returns outputStream
        
        handler.handle(exchange)
        
        verify { exchange.sendResponseHeaders(405, any()) }
    }
    
    private fun createMockExchange(method: String): HttpExchange {
        val exchange = mockk<HttpExchange>(relaxed = true)
        every { exchange.requestMethod } returns method
        every { exchange.responseHeaders } returns mockk(relaxed = true)
        return exchange
    }
}

