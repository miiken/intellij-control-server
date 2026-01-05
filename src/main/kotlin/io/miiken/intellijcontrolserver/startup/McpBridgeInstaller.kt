package io.miiken.intellijcontrolserver.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission

/**
 * Automatically installs the MCP bridge wrapper script on first run
 * 
 * This makes installation much simpler for users:
 * 1. Install plugin from marketplace
 * 2. Restart IntelliJ
 * 3. Copy the fixed Cursor config from the notification
 * 4. Done!
 */
class McpBridgeInstaller : StartupActivity {
    private val logger = Logger.getInstance(McpBridgeInstaller::class.java)
    
    override fun runActivity(project: Project) {
        ApplicationManager.getApplication().executeOnPooledThread {
            installMcpBridge()
        }
    }
    
    private fun installMcpBridge() {
        try {
            val bridgeDir = File(System.getProperty("user.home"), ".intellij-mcp-bridge")
            val wrapperScript = File(bridgeDir, "bridge.sh")
            val installMarker = File(bridgeDir, ".installed-version")
            
            val pluginVersion = javaClass.`package`.implementationVersion ?: "1.0.0"
            
            val isFirstInstall = !wrapperScript.exists()
            val needsUpgrade = !installMarker.exists() || installMarker.readText().trim() != pluginVersion
            
            if (wrapperScript.exists() && !needsUpgrade) {
                logger.info("MCP bridge wrapper already installed (version $pluginVersion)")
                return
            }
            
            if (isFirstInstall) {
                logger.info("Installing MCP bridge wrapper (version $pluginVersion)...")
            } else {
                logger.info("Updating MCP bridge wrapper to version $pluginVersion...")
            }
            
            bridgeDir.mkdirs()
            
            val resourceStream = javaClass.classLoader.getResourceAsStream("mcp-bridge-wrapper.sh")
                ?: run {
                    logger.error("Could not find mcp-bridge-wrapper.sh resource")
                    return
                }
            
            resourceStream.use { input ->
                Files.copy(input, wrapperScript.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            
            try {
                val perms = setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE
                )
                Files.setPosixFilePermissions(wrapperScript.toPath(), perms)
            } catch (e: UnsupportedOperationException) {
                logger.info("POSIX permissions not supported (Windows), using setExecutable")
                wrapperScript.setExecutable(true, false)
            }
            
            installMarker.writeText(pluginVersion)
            
            logger.info("MCP bridge wrapper installed successfully (version $pluginVersion)")
            
            if (isFirstInstall) {
                showInstallationNotification(wrapperScript.absolutePath, isUpgrade = false)
            }
            
        } catch (e: Exception) {
            logger.error("Failed to install MCP bridge wrapper", e)
        }
    }
    
    private fun showInstallationNotification(wrapperPath: String, isUpgrade: Boolean) {
        val cursorConfig = buildCursorConfig(wrapperPath)
        
        val title = if (isUpgrade) {
            "IntelliJ Control Server - MCP Bridge Updated"
        } else {
            "IntelliJ Control Server - MCP Bridge Ready"
        }
        
        ApplicationManager.getApplication().invokeLater {
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("IntelliJ Control Server")
                .createNotification(
                    title,
                    buildNotificationContent(cursorConfig),
                    NotificationType.INFORMATION
                )
            
            notification.notify(null)
        }
    }
    
    private fun buildCursorConfig(wrapperPath: String): String {
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        val configPath = if (isWindows) wrapperPath.replace("/", "\\") else wrapperPath
        
        return """
        {
          "mcpServers": {
            "intellij": {
              "command": "$configPath"
            }
          }
        }
        """.trimIndent()
    }
    
    private fun buildNotificationContent(cursorConfig: String): String {
        val wrapperPath = File(System.getProperty("user.home"), ".intellij-mcp-bridge/bridge.sh").absolutePath
        
        return """
            <html><body>
            <p><b>MCP Bridge installed successfully!</b></p>
            <p><b>Location:</b> <code>$wrapperPath</code></p>
            <p><b>To enable in Cursor AI:</b></p>
            <ol>
              <li>Open Cursor settings (Cmd+Shift+J or Ctrl+Shift+J)</li>
              <li>Find the "mcpServers" section</li>
              <li>Add the "intellij" entry (see below)</li>
              <li>Restart Cursor</li>
            </ol>
            <p><b>Configuration to add:</b></p>
            <pre>$cursorConfig</pre>
            <p><i>Tip: This won't affect your other MCP servers.</i></p>
            </body></html>
        """.trimIndent()
    }
}

