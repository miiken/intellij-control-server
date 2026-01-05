package io.miiken.intellijcontrolserver.mcp.tools

import com.intellij.openapi.project.ProjectManager
import io.miiken.intellijcontrolserver.mcp.Tool
import io.miiken.intellijcontrolserver.models.RenameRequest
import io.miiken.intellijcontrolserver.services.RefactoringService

/**
 * Rename symbol tool for MCP
 * 
 * Renames a symbol (class, method, variable, parameter) in the codebase.
 */
class RenameSymbolTool : Tool {
    override val name = "intellij_rename_symbol"
    
    override val description = "Rename a symbol (class, method, variable, parameter) in the codebase"
    
    override val inputSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "projectName" to mapOf(
                "type" to "string",
                "description" to "Name of the IntelliJ project"
            ),
            "filePath" to mapOf(
                "type" to "string",
                "description" to "Path to file (relative to project root)"
            ),
            "line" to mapOf(
                "type" to "number",
                "description" to "Line number where the symbol is located (1-based)"
            ),
            "oldName" to mapOf(
                "type" to "string",
                "description" to "Current name of the symbol"
            ),
            "newName" to mapOf(
                "type" to "string",
                "description" to "New name for the symbol"
            )
        ),
        "required" to listOf("projectName", "filePath", "line", "oldName", "newName")
    )
    
    override fun execute(arguments: Map<String, Any>): Any {
        val projectName = arguments["projectName"] as? String
            ?: throw IllegalArgumentException("projectName is required")
        val filePath = arguments["filePath"] as? String
            ?: throw IllegalArgumentException("filePath is required")
        val line = (arguments["line"] as? Number)?.toInt()
            ?: throw IllegalArgumentException("line is required")
        val oldName = arguments["oldName"] as? String
            ?: throw IllegalArgumentException("oldName is required")
        val newName = arguments["newName"] as? String
            ?: throw IllegalArgumentException("newName is required")
        
        val project = ProjectManager.getInstance().openProjects.firstOrNull { it.name == projectName }
            ?: throw IllegalArgumentException("Project not found: $projectName")
        
        val request = RenameRequest(
            filePath = filePath,
            line = line,
            oldName = oldName,
            newName = newName
        )
        
        val result = RefactoringService.rename(project, request)
        
        if (!result.success) {
            throw RuntimeException(result.error?.message ?: "Rename failed")
        }
        
        return result
    }
}

