package io.hibob.intellijcontrolserver

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import io.hibob.intellijcontrolserver.config.ConfigLoader
import io.hibob.intellijcontrolserver.config.ServerConfig

/**
 * Application-level service for Control Server
 * This service is a singleton that manages the HTTP server lifecycle
 */
@Service
class ControlServerService : Disposable {
    private val logger = Logger.getInstance(ControlServerService::class.java)
    private var controlServer: ControlServer? = null
    private var config: ServerConfig = ServerConfig.DEFAULT
    
    init {
        logger.info("ControlServerService initialized")
    }
    
    /**
     * Start the control server
     */
    fun startServer() {
        if (controlServer?.isRunning() == true) {
            logger.warn("Control Server is already running")
            return
        }
        
        try {
            // Load configuration
            config = ConfigLoader.load()
            logger.info("Loaded configuration: port=${config.port}, host=${config.host}")
            
            // Create and start server
            controlServer = ControlServer(config).apply {
                start()
            }
            
        } catch (e: Exception) {
            logger.error("Failed to start Control Server", e)
            // Don't throw - we don't want to prevent IntelliJ from starting
        }
    }
    
    /**
     * Stop the control server
     */
    fun stopServer() {
        try {
            controlServer?.stop()
            controlServer = null
        } catch (e: Exception) {
            logger.error("Error stopping Control Server", e)
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

