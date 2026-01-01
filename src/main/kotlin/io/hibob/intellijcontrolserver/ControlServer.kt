package io.hibob.intellijcontrolserver

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.sun.net.httpserver.HttpServer
import io.hibob.intellijcontrolserver.config.ServerConfig
import io.hibob.intellijcontrolserver.server.handlers.HealthHandler
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/**
 * Main HTTP server for IntelliJ Control Server
 */
class ControlServer(private val config: ServerConfig) : Disposable {
    private val logger = Logger.getInstance(ControlServer::class.java)
    private var server: HttpServer? = null
    private val startTime = System.currentTimeMillis()
    
    /**
     * Start the HTTP server
     */
    fun start() {
        try {
            logger.info("Starting IntelliJ Control Server...")
            logger.info("Configuration: port=${config.port}, host=${config.host}, cors=${config.enableCors}")
            
            // Create HTTP server
            val address = InetSocketAddress(config.host, config.port)
            server = HttpServer.create(address, 0).apply {
                // Set thread pool for handling requests
                executor = Executors.newFixedThreadPool(10)
                
                // Register handlers
                registerHandlers(this)
                
                // Start server
                start()
            }
            
            logger.info("✓ IntelliJ Control Server started successfully on http://${config.host}:${config.port}")
            logger.info("  Health check: http://${config.host}:${config.port}/health")
            
        } catch (e: Exception) {
            logger.error("Failed to start IntelliJ Control Server", e)
            throw RuntimeException("Failed to start Control Server: ${e.message}", e)
        }
    }
    
    /**
     * Stop the HTTP server
     */
    fun stop() {
        try {
            logger.info("Stopping IntelliJ Control Server...")
            server?.stop(0)
            server = null
            logger.info("✓ IntelliJ Control Server stopped")
        } catch (e: Exception) {
            logger.error("Error stopping Control Server", e)
        }
    }
    
    /**
     * Check if server is running
     */
    fun isRunning(): Boolean = server != null
    
    /**
     * Get server port
     */
    fun getPort(): Int = config.port
    
    /**
     * Get server host
     */
    fun getHost(): String = config.host
    
    /**
     * Register HTTP handlers
     */
    private fun registerHandlers(server: HttpServer) {
        logger.info("Registering HTTP handlers...")
        
        // Health check endpoint
        server.createContext("/health", HealthHandler(startTime))
        
        // TODO: Register additional handlers as they are implemented
        // server.createContext("/tasks", TasksHandler())
        // server.createContext("/refactor", RefactoringHandler())
        // server.createContext("/navigation", NavigationHandler())
        
        logger.info("✓ Registered health check handler at /health")
    }
    
    /**
     * Dispose method for IntelliJ's Disposable interface
     */
    override fun dispose() {
        stop()
    }
}

