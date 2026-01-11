package io.miiken.intellijcontrolserver.mcp.tools

import com.intellij.openapi.project.ProjectManager
import io.miiken.intellijcontrolserver.mcp.Tool
import io.miiken.intellijcontrolserver.mcp.ToolRegistry
import io.miiken.intellijcontrolserver.models.ExtractMethodAnalysis
import io.miiken.intellijcontrolserver.models.ExtractMethodRequest
import io.miiken.intellijcontrolserver.services.RefactoringService
import kotlin.reflect.KClass

/**
 * MCP tool for analyzing code for extract method refactoring (Phase 1).
 * 
 * This tool analyzes selected code and returns suggestions without making any changes.
 */
class AnalyzeExtractMethodTool private constructor() : Tool<ExtractMethodRequest, ExtractMethodAnalysis> {
    override val name = "intellij_analyze_extract_method"
    
    override val description = "Analyze code selection for extract method refactoring without making changes. Returns suggestions for method name, parameters, and return type."
    
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
            )
        ),
        "required" to listOf("projectName", "filePath", "startLine", "endLine")
    )
    
    override val inputClass: KClass<ExtractMethodRequest> = ExtractMethodRequest::class
    
    override fun execute(request: ExtractMethodRequest): ExtractMethodAnalysis {
        val projectName = request.projectName
            ?: throw IllegalArgumentException("projectName is required")
        
        val project = ProjectManager.getInstance().openProjects.firstOrNull { it.name == projectName }
            ?: throw IllegalArgumentException("Project not found: $projectName")
        
        val analysis = RefactoringService.analyzeExtractMethod(project, request)
        
        if (!analysis.canExtract && analysis.errorMessage != null) {
            throw RuntimeException("Analysis failed: ${analysis.errorMessage}")
        }
        
        return analysis
    }
    
    companion object {
        init {
            ToolRegistry.register(AnalyzeExtractMethodTool())
        }
    }
}
