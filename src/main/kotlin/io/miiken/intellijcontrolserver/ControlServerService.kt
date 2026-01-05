package io.miiken.intellijcontrolserver

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import io.miiken.intellijcontrolserver.config.ConfigLoader
import io.miiken.intellijcontrolserver.config.ServerConfig
import io.miiken.intellijcontrolserver.mcp.McpStdioServer

/**
 * Application-level service for Control Server
 * This service is a singleton that manages the HTTP and MCP server lifecycle
 */
@Service
class ControlServerService : Disposable {
    private val logger = Logger.getInstance(ControlServerService::class.java)
    private var controlServer: ControlServer? = null
    private var mcpServer: McpStdioServer? = null
    private var config: ServerConfig = ServerConfig.DEFAULT
    
    init {
        logger.info("ControlServerService initialized")
    }
    
    /**
     * Start the control server and MCP server
     */
    fun startServer() {
        if (controlServer?.isRunning() == true) {
            logger.warn("Control Server is already running")
            return
        }
        
        try {
            loadConfiguration()
            createAndStartServers()
        } catch (e: Exception) {
            logger.error("Failed to start servers", e)
        }
    }
    
    private fun loadConfiguration() {
        config = ConfigLoader.load()
        logger.info("Loaded configuration: port=${config.port}, host=${config.host}")
    }
    
    private fun createAndStartServers() {
        controlServer = ControlServer(config).apply {
            start()
        }
        
        if (config.enableMcp) {
            mcpServer = McpStdioServer().apply {
                start()
            }
            logger.info("MCP server started")
        }
    }
    
    /**
     * Stop the control server and MCP server
     */
    fun stopServer() {
        try {
            controlServer?.stop()
            controlServer = null
            
            mcpServer?.stop()
            mcpServer = null
        } catch (e: Exception) {
            logger.error("Error stopping servers", e)
        }
    }
    
    /**
     * Restart the control server (useful for config changes)
     */
    fun restartServer() {
        logger.info("Restarting Control Server...")
        stopServer()
        startServer()
    }
    
    /**
     * Check if server is running
     */
    fun isRunning(): Boolean = controlServer?.isRunning() == true
    
    /**
     * Get server URL
     */
    fun getServerUrl(): String? {
        return controlServer?.let { "http://${it.getHost()}:${it.getPort()}" }
    }
    
    /**
     * Get current configuration
     */
    fun getConfig(): ServerConfig = config
    
    /**
     * Update configuration and restart server
     */
    fun updateConfig(newConfig: ServerConfig) {
        config = newConfig
        if (isRunning()) {
            restartServer()
        }
    }
    
    override fun dispose() {
        logger.info("Disposing ControlServerService")
        stopServer()
    }
    
    companion object {
        /**
         * Get the singleton instance
         */
        fun getInstance(): ControlServerService {
            return com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(ControlServerService::class.java)
        }
    }
}

