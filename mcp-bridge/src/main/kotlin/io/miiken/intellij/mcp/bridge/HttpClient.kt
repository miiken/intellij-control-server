package io.miiken.intellij.mcp.bridge

import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.reflect.KClass

/**
 * HTTP client for making requests to the IntelliJ plugin
 */
object PluginHttpClient {
    private val client = HttpClient.newBuilder().build()
    private val gson = Gson()
    
    /**
     * Make GET request
     */
    fun <T : Any> get(url: String, responseClass: KClass<T>): T {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .header("Content-Type", "application/json")
            .build()
        
        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            
            if (response.statusCode() != 200) {
                throw RuntimeException("HTTP ${response.statusCode()}: ${response.body()}")
            }
            
            return gson.fromJson(response.body(), responseClass.java)
        } catch (e: java.net.ConnectException) {
            throw RuntimeException("Cannot connect to IntelliJ plugin at $url. Is IntelliJ Control Server plugin running?", e)
        }
    }
    
    /**
     * Make POST request
     */
    fun <T : Any> post(url: String, body: Any, responseClass: KClass<T>): T {
        val jsonBody = gson.toJson(body)
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build()
        
        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            
            if (response.statusCode() != 200) {
                throw RuntimeException("HTTP ${response.statusCode()}: ${response.body()}")
            }
            
            return gson.fromJson(response.body(), responseClass.java)
        } catch (e: java.net.ConnectException) {
            throw RuntimeException("Cannot connect to IntelliJ plugin at $url. Is IntelliJ Control Server plugin running?", e)
        }
    }
}

