package io.miiken.intellijcontrolserver.mcp.tools

import com.intellij.openapi.project.ProjectManager
import io.miiken.intellijcontrolserver.mcp.Tool
import io.miiken.intellijcontrolserver.mcp.ToolRegistry
import io.miiken.intellijcontrolserver.models.ExtractMethodRequest
import io.miiken.intellijcontrolserver.models.RefactoringResult
import io.miiken.intellijcontrolserver.services.RefactoringService
import kotlin.reflect.KClass

/**
 * Extract method tool for MCP
 * 
 * Extracts selected code into a new method.
 */
class ExtractMethodTool private constructor() : Tool<ExtractMethodRequest, RefactoringResult> {
    override val name = "intellij_extract_method"
    
    override val description = "Extract selected code into a new method"
    
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
            "startLine" to mapOf(
                "type" to "number",
                "description" to "Start line of code selection (1-based)"
            ),
            "endLine" to mapOf(
                "type" to "number",
                "description" to "End line of code selection (1-based)"
            ),
            "startColumn" to mapOf(
                "type" to "number",
                "description" to "Start column on startLine (0-based, optional)"
            ),
            "endColumn" to mapOf(
                "type" to "number",
                "description" to "End column on endLine (0-based, optional)"
            ),
            "methodName" to mapOf(
                "type" to "string",
                "description" to "Name for the extracted method"
            ),
            "visibility" to mapOf(
                "type" to "string",
                "enum" to listOf("private", "protected", "public", "internal"),
                "description" to "Visibility modifier for the method",
                "default" to "private"
            )
        ),
        "required" to listOf("projectName", "filePath", "startLine", "endLine", "methodName")
    )
    
    override val inputClass: KClass<ExtractMethodRequest> = ExtractMethodRequest::class
    
    override fun execute(request: ExtractMethodRequest): RefactoringResult {
        val projectName = request.projectName
            ?: throw IllegalArgumentException("projectName is required")
        
        val project = ProjectManager.getInstance().openProjects.firstOrNull { it.name == projectName }
            ?: throw IllegalArgumentException("Project not found: $projectName")
        
        val result = RefactoringService.extractMethod(project, request)
        
        if (!result.success) {
            throw RuntimeException(result.error?.message ?: "Extract method failed")
        }
        
        return result
    }
    
    companion object {
        init {
            ToolRegistry.register(ExtractMethodTool())
        }
    }
}

