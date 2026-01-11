package io.miiken.intellijcontrolserver.mcp.tools

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import io.miiken.intellijcontrolserver.mcp.Tool
import io.miiken.intellijcontrolserver.mcp.ToolRegistry
import io.miiken.intellijcontrolserver.mcp.models.EmptyRequest
import io.miiken.intellijcontrolserver.models.RefactoringResult
import kotlin.reflect.KClass

/**
 * MCP tool for restarting IntelliJ IDEA
 */
class RestartIdeTool private constructor() : Tool<EmptyRequest, RefactoringResult> {
    private val logger = Logger.getInstance(RestartIdeTool::class.java)
    
    override val name = "intellij_restart"
    
    override val description = "Restart IntelliJ IDEA. The IDE will be unavailable for ~10-30 seconds."
    
    override val inputSchema = mapOf(
        "type" to "object",
        "properties" to emptyMap<String, Any>(),
        "required" to emptyList<String>()
    )
    
    override val inputClass: KClass<EmptyRequest> = EmptyRequest::class
    
    override fun execute(request: EmptyRequest): RefactoringResult {
        logger.info("Received request to restart IntelliJ IDEA via MCP.")
        
        ApplicationManager.getApplication().invokeLater {
            try {
                val ideRestartCommand = getIdeRestartCommand()
                if (ideRestartCommand != null) {
                    logger.info("Executing IDE restart command: ${ideRestartCommand.joinToString(" ")}")
                    Runtime.getRuntime().exec(ideRestartCommand)
                } else {
                    logger.warn("Could not determine IDE restart command for current OS. Manual restart required.")
                }
            } catch (e: Exception) {
                logger.error("Failed to restart IDE programmatically", e)
            }
        }
        
        return RefactoringResult(
            success = true
        )
    }
    
    private fun getIdeRestartCommand(): Array<String>? {
        if (SystemInfo.isMac) {
            val appPath = System.getProperty("idea.executable")
                ?: "/Applications/IntelliJ IDEA Ultimate.app/Contents/MacOS/idea"
            return arrayOf("open", "-a", appPath)
        }
        return null
    }
    
    companion object {
        init {
            ToolRegistry.register(RestartIdeTool())
        }
    }
}
