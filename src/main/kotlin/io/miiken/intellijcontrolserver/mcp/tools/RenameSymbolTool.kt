package io.miiken.intellijcontrolserver.mcp.tools

import com.intellij.openapi.project.ProjectManager
import io.miiken.intellijcontrolserver.mcp.Tool
import io.miiken.intellijcontrolserver.mcp.ToolRegistry
import io.miiken.intellijcontrolserver.models.RefactoringResult
import io.miiken.intellijcontrolserver.models.RenameRequest
import io.miiken.intellijcontrolserver.services.RefactoringService
import kotlin.reflect.KClass

/**
 * Rename symbol tool for MCP
 *
 * Renames a symbol (class, method, variable, parameter) in the codebase.
 */
class RenameSymbolTool private constructor() : Tool<RenameRequest, RefactoringResult> {
    override val name = "intellij_rename_symbol"

    override val description = "Rename a symbol (class, method, variable, parameter) in the codebase"

    override val inputSchema =
        mapOf(
            "type" to "object",
            "properties" to
                mapOf(
                    "projectName" to
                        mapOf(
                            "type" to "string",
                            "description" to "Name of the IntelliJ project",
                        ),
                    "filePath" to
                        mapOf(
                            "type" to "string",
                            "description" to "Path to file (relative to project root)",
                        ),
                    "line" to
                        mapOf(
                            "type" to "number",
                            "description" to "Line number where the symbol is located (1-based)",
                        ),
                    "oldName" to
                        mapOf(
                            "type" to "string",
                            "description" to "Current name of the symbol",
                        ),
                    "newName" to
                        mapOf(
                            "type" to "string",
                            "description" to "New name for the symbol",
                        ),
                ),
            "required" to listOf("projectName", "filePath", "line", "oldName", "newName"),
        )

    override val inputClass: KClass<RenameRequest> = RenameRequest::class

    override fun execute(request: RenameRequest): RefactoringResult {
        val projectName =
            request.projectName
                ?: throw IllegalArgumentException("projectName is required")

        val project =
            ProjectManager.getInstance().openProjects.firstOrNull { it.name == projectName }
                ?: throw IllegalArgumentException("Project not found: $projectName")

        val result = RefactoringService.rename(project, request)

        if (!result.success) {
            throw RuntimeException(result.error?.message ?: "Rename failed")
        }

        return result
    }

    companion object {
        init {
            ToolRegistry.register(RenameSymbolTool())
        }
    }
}
