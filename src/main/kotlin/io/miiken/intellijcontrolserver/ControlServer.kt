package io.miiken.intellijcontrolserver

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager
import com.sun.net.httpserver.HttpServer
import io.miiken.intellijcontrolserver.config.ServerConfig
import io.miiken.intellijcontrolserver.server.controllers.ApiDocsController
import io.miiken.intellijcontrolserver.server.controllers.HealthController
import io.miiken.intellijcontrolserver.server.controllers.McpController
import io.miiken.intellijcontrolserver.server.controllers.RefactoringController
import io.miiken.intellijcontrolserver.server.openapi.OpenApiGenerator
import io.miiken.intellijcontrolserver.server.routing.ControllerDispatcher
import io.miiken.intellijcontrolserver.server.routing.ControllerRegistry
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
            logStartupInfo()
            server = createAndConfigureHttpServer()
            logSuccessfulStartup()
        } catch (e: Exception) {
            logger.error("Failed to start IntelliJ Control Server", e)
            throw RuntimeException("Failed to start Control Server: ${e.message}", e)
        }
    }
    
    private fun logStartupInfo() {
        logger.info("Starting IntelliJ Control Server...")
        logger.info("Configuration: port=${config.port}, host=${config.host}, cors=${config.enableCors}")
    }
    
    private fun createAndConfigureHttpServer(): HttpServer {
        val address = InetSocketAddress(config.host, config.port)
        return HttpServer.create(address, 0).apply {
            executor = Executors.newFixedThreadPool(10)
            registerHandlers(this)
            start()
        }
    }
    
    private fun logSuccessfulStartup() {
        logger.info("✓ IntelliJ Control Server started successfully on http://${config.host}:${config.port}")
        logger.info("  Health check: http://${config.host}:${config.port}/health")
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
        logger.info("Registering annotation-based controllers...")
        
        val registry = ControllerRegistry()
        
        registry.registerController(HealthController(startTime))
        registry.registerController(RefactoringController())
        registry.registerController(McpController())
        
        val openProjects = ProjectManager.getInstance().openProjects
        if (openProjects.isNotEmpty()) {
            logger.info("✓ Registered RefactoringController (dynamic project resolution)")
            logger.info("  Available projects: ${openProjects.joinToString(", ") { it.name }}")
            openProjects.forEach { project ->
                logger.info("  - /${project.name}/refactor/rename")
                logger.info("  - /${project.name}/refactor/extract-method")
            }
        } else {
            logger.warn("⚠ No open projects yet")
            logger.info("  RefactoringController will resolve projects dynamically at runtime")
            logger.info("  Endpoints: /{projectName}/refactor/rename and /{projectName}/refactor/extract-method")
        }
        
        val openApiGenerator = OpenApiGenerator(registry)
        registry.registerController(ApiDocsController(openApiGenerator))
        
        val dispatcher = ControllerDispatcher(registry)
        server.createContext("/", dispatcher)
        
        logger.info("✓ Registered ${registry.getRoutes().size} routes:")
        registry.getRoutes().forEach { route ->
            logger.info("  ${route.method} ${route.path}")
        }
    }
    
    /**
     * Dispose method for IntelliJ's Disposable interface
     */
    override fun dispose() {
        stop()
    }
}

