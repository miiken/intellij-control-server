package io.miiken.intellijcontrolserver.mcp.tools

import com.intellij.openapi.project.ProjectManager
import io.miiken.intellijcontrolserver.mcp.Tool
import io.miiken.intellijcontrolserver.mcp.ToolRegistry
import io.miiken.intellijcontrolserver.models.ExtractMethodExecuteRequest
import io.miiken.intellijcontrolserver.models.ExtractMethodOptions
import io.miiken.intellijcontrolserver.models.ExtractMethodRequest
import io.miiken.intellijcontrolserver.models.RefactoringResult
import io.miiken.intellijcontrolserver.services.RefactoringService
import kotlin.reflect.KClass

/**
 * MCP tool for executing extract method refactoring with options (Phase 2).
 * 
 * This tool performs the actual extraction using the options provided after analysis.
 */
class ExecuteExtractMethodTool private constructor() : Tool<ExtractMethodExecuteRequest, RefactoringResult> {
    override val name = "intellij_execute_extract_method"
    
    override val description = "Execute extract method refactoring with user-provided options. Use after analyzing with intellij_analyze_extract_method."
    
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
            "parameterOrder" to mapOf(
                "type" to "array",
                "items" to mapOf("type" to "string"),
                "description" to "Optional order of parameters (use names from analysis)"
            ),
            "visibility" to mapOf(
                "type" to "string",
                "enum" to listOf("private", "protected", "public", "internal"),
                "description" to "Visibility modifier for the method",
                "default" to "private"
            ),
            "isStatic" to mapOf(
                "type" to "boolean",
                "description" to "Whether the method should be static",
                "default" to false
            ),
            "returnType" to mapOf(
                "type" to "string",
                "description" to "Optional explicit return type (uses inferred if omitted)"
            )
        ),
        "required" to listOf("projectName", "filePath", "startLine", "endLine", "methodName")
    )
    
    override val inputClass: KClass<ExtractMethodExecuteRequest> = ExtractMethodExecuteRequest::class
    
    override fun execute(request: ExtractMethodExecuteRequest): RefactoringResult {
        // For MCP, we need to get projectName from somewhere - let's use a synthetic field
        // or infer it from open projects
        val projectName = ProjectManager.getInstance().openProjects.firstOrNull()?.name
            ?: throw IllegalArgumentException("No open projects found")
        
        val project = ProjectManager.getInstance().openProjects.first()
        
        // Convert to base request and options
        val baseRequest = ExtractMethodRequest(
            projectName = projectName,
            filePath = request.filePath,
            startLine = request.startLine,
            endLine = request.endLine,
            startColumn = request.startColumn,
            endColumn = request.endColumn,
            methodName = request.methodName
        )
        
        val options = ExtractMethodOptions(
            methodName = request.methodName,
            parameterOrder = request.parameterOrder,
            visibility = request.visibility,
            isStatic = request.isStatic,
            returnType = request.returnType
        )
        
        val result = RefactoringService.executeExtractMethodWithOptions(project, baseRequest, options)
        
        if (!result.success) {
            throw RuntimeException(result.error?.message ?: "Extract method failed")
        }
        
        return result
    }
    
    companion object {
        init {
            ToolRegistry.register(ExecuteExtractMethodTool())
        }
    }
}
