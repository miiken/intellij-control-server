package io.miiken.intellijcontrolserver

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.diagnostic.Logger

/**
 * Application lifecycle listener
 * Starts the Control Server when IntelliJ starts
 */
class ControlServerApplicationListener : AppLifecycleListener {
    private val logger = Logger.getInstance(ControlServerApplicationListener::class.java)
    
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        logger.info("IntelliJ frame created, initializing Control Server...")
        
        try {
            val service = ControlServerService.getInstance()
            val config = service.getConfig()
            
            if (config.autoStart) {
                logger.info("Auto-start is enabled, starting Control Server...")
                service.startServer()
            } else {
                logger.info("Auto-start is disabled, Control Server not started")
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize Control Server", e)
            // Don't throw - we don't want to crash IntelliJ
        }
    }
    
    override fun appWillBeClosed(isRestart: Boolean) {
        logger.info("IntelliJ is closing (restart=$isRestart), stopping Control Server...")
        
        try {
            val service = ControlServerService.getInstance()
            service.stopServer()
        } catch (e: Exception) {
            logger.error("Error stopping Control Server on shutdown", e)
        }
    }
}

