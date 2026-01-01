package io.miiken.intellijcontrolserver.server

import com.google.gson.Gson
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpPrincipal
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResponseBuilderTest {
    
    private val gson = Gson()
    
    @Test
    fun `should send success response with data`() {
        val outputStream = ByteArrayOutputStream()
        val exchange = createTestExchange(outputStream)
        
        val data = mapOf("key" to "value", "number" to 42)
        ResponseBuilder.sendSuccess(exchange, data)
        
        val response = outputStream.toString()
        val json = gson.fromJson(response, Map::class.java)
        
        assertEquals("value", json["key"])
        assertEquals(42.0, json["number"])
    }
    
    @Test
    fun `should send error response with code and message`() {
        val outputStream = ByteArrayOutputStream()
        val exchange = createTestExchange(outputStream)
        
        ResponseBuilder.sendError(exchange, "TEST_ERROR", "Test error message")
        
        val response = outputStream.toString()
        val json = gson.fromJson(response, Map::class.java)
        
        assertEquals(false, json["success"])
        @Suppress("UNCHECKED_CAST")
        val error = json["error"] as Map<String, Any>
        assertEquals("TEST_ERROR", error["code"])
        assertEquals("Test error message", error["message"])
    }
    
    @Test
    fun `should send error response with details`() {
        val outputStream = ByteArrayOutputStream()
        val exchange = createTestExchange(outputStream)
        
        val details = mapOf("field" to "username", "constraint" to "min_length")
        ResponseBuilder.sendError(exchange, "VALIDATION_ERROR", "Invalid input", details)
        
        val response = outputStream.toString()
        val json = gson.fromJson(response, Map::class.java)
        
        assertEquals(false, json["success"])
        @Suppress("UNCHECKED_CAST")
        val error = json["error"] as Map<String, Any>
        assertEquals("VALIDATION_ERROR", error["code"])
        assertEquals("Invalid input", error["message"])
        
        @Suppress("UNCHECKED_CAST")
        val responseDetails = error["details"] as Map<String, Any>
        assertEquals("username", responseDetails["field"])
        assertEquals("min_length", responseDetails["constraint"])
    }
    
    @Test
    fun `should send text response`() {
        val outputStream = ByteArrayOutputStream()
        val exchange = createTestExchange(outputStream)
        
        val textContent = "Hello, World!"
        ResponseBuilder.sendText(exchange, textContent)
        
        val response = outputStream.toString()
        assertEquals(textContent, response)
    }
    
    @Test
    fun `should send response with custom status code`() {
        val outputStream = ByteArrayOutputStream()
        val exchange = createTestExchange(outputStream)
        
        ResponseBuilder.sendSuccess(exchange, mapOf("data" to "test"), 201)
        
        assertEquals(201, (exchange as TestHttpExchange).statusCode)
    }
    
    @Test
    fun `should set CORS headers when enabled`() {
        val outputStream = ByteArrayOutputStream()
        val exchange = createTestExchange(outputStream)
        
        ResponseBuilder.setCorsHeaders(exchange, enableCors = true)
        
        val headers = exchange.responseHeaders
        assertTrue(headers.containsKey("Access-Control-Allow-Origin"))
        assertTrue(headers.containsKey("Access-Control-Allow-Methods"))
        assertTrue(headers.containsKey("Access-Control-Allow-Headers"))
    }
    
    @Test
    fun `should not set CORS headers when disabled`() {
        val outputStream = ByteArrayOutputStream()
        val exchange = createTestExchange(outputStream)
        
        ResponseBuilder.setCorsHeaders(exchange, enableCors = false)
        
        val headers = exchange.responseHeaders
        assertTrue(!headers.containsKey("Access-Control-Allow-Origin"))
    }
    
    private fun createTestExchange(outputStream: OutputStream): HttpExchange {
        return TestHttpExchange(outputStream)
    }
    
    private class TestHttpExchange(
        private val outputStream: OutputStream
    ) : HttpExchange() {
        private val requestHeaders = Headers()
        private val responseHeadersMap = Headers()
        var statusCode: Int = 0
        private var responseLength: Long = 0
        
        override fun getRequestMethod(): String = "GET"
        override fun getRequestURI(): URI = URI.create("http://localhost:8080/test")
        override fun getProtocol(): String = "HTTP/1.1"
        override fun getRequestHeaders(): Headers = requestHeaders
        override fun getResponseHeaders(): Headers = responseHeadersMap
        override fun getRequestBody(): InputStream = ByteArrayInputStream(ByteArray(0))
        override fun getResponseBody(): OutputStream = outputStream
        override fun getRemoteAddress(): InetSocketAddress = InetSocketAddress("localhost", 12345)
        override fun getResponseCode(): Int = statusCode
        override fun getLocalAddress(): InetSocketAddress = InetSocketAddress("localhost", 8080)
        override fun getHttpContext(): HttpContext? = null
        override fun getPrincipal(): HttpPrincipal? = null
        override fun getAttribute(name: String?): Any? = null
        override fun setAttribute(name: String?, value: Any?) {}
        override fun setStreams(i: InputStream?, o: OutputStream?) {}
        
        override fun sendResponseHeaders(rCode: Int, responseLength: Long) {
            this.statusCode = rCode
            this.responseLength = responseLength
        }
        
        override fun close() {
            outputStream.close()
        }
    }
}
