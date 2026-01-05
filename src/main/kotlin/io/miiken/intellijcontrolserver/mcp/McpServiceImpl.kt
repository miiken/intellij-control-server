package io.miiken.intellijcontrolserver.mcp

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager
import io.miiken.intellijcontrolserver.ControlServerService
import io.miiken.intellijcontrolserver.models.ExtractMethodRequest
import io.miiken.intellijcontrolserver.models.RenameRequest
import io.miiken.intellijcontrolserver.services.RefactoringService

/**
 * Implementation of MCP Service
 * 
 * Handles MCP protocol methods and delegates to appropriate services
 */
class McpServiceImpl : McpService {
    private val logger = Logger.getInstance(McpServiceImpl::class.java)
    private val gson = Gson()
    
    override fun initialize(params: Map<String, Any>?): InitializeResponse {
        logger.info("MCP initialize called")
        return InitializeResponse(
            protocolVersion = "2024-11-05",
            serverInfo = ServerInfo(
                name = "intellij-control-server",
                version = "1.0.0"
            ),
            capabilities = Capabilities(
                tools = emptyMap()
            )
        )
    }
    
    override fun listTools(): ToolsListResponse {
        logger.info("MCP tools/list called")
        
        val tools = listOf(
            createHealthCheckTool(),
            createRenameSymbolTool(),
            createExtractMethodTool()
        )
        
        return ToolsListResponse(tools)
    }
    
    override fun callTool(name: String, arguments: Map<String, Any>?): ToolCallResponse {
        logger.info("MCP tools/call: $name")
        
        val result = when (name) {
            "intellij_health_check" -> executeHealthCheck()
            "intellij_rename_symbol" -> executeRename(arguments ?: emptyMap())
            "intellij_extract_method" -> executeExtractMethod(arguments ?: emptyMap())
            else -> throw IllegalArgumentException("Unknown tool: $name")
        }
        
        return ToolCallResponse(
            content = listOf(
                ContentItem(
                    type = "text",
                    text = gson.toJson(result)
                )
            )
        )
    }
    
    private fun createHealthCheckTool(): ToolDefinition {
        return ToolDefinition(
            name = "intellij_health_check",
            description = "Check if the IntelliJ Control Server is running and get status information including uptime",
            inputSchema = mapOf(
                "type" to "object",
                "properties" to emptyMap<String, Any>(),
                "required" to emptyList<String>()
            )
        )
    }
    
    private fun createRenameSymbolTool(): ToolDefinition {
        return ToolDefinition(
            name = "intellij_rename_symbol",
            description = "Rename a symbol (class, method, variable, parameter) in the codebase",
            inputSchema = mapOf(
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
        )
    }
    
    private fun createExtractMethodTool(): ToolDefinition {
        return ToolDefinition(
            name = "intellij_extract_method",
            description = "Extract selected code into a new method",
            inputSchema = mapOf(
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
        )
    }
    
    private fun executeHealthCheck(): Any {
        val service = ControlServerService.getInstance()
        
        return if (service.isRunning()) {
            mapOf(
                "status" to "ok",
                "version" to "1.0.0",
                "timestamp" to System.currentTimeMillis()
            )
        } else {
            mapOf(
                "status" to "stopped",
                "message" to "Server is not running"
            )
        }
    }
    
    private fun executeRename(arguments: Map<String, Any>): Any {
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
    
    private fun executeExtractMethod(arguments: Map<String, Any>): Any {
        val projectName = arguments["projectName"] as? String
            ?: throw IllegalArgumentException("projectName is required")
        val filePath = arguments["filePath"] as? String
            ?: throw IllegalArgumentException("filePath is required")
        val startLine = (arguments["startLine"] as? Number)?.toInt()
            ?: throw IllegalArgumentException("startLine is required")
        val endLine = (arguments["endLine"] as? Number)?.toInt()
            ?: throw IllegalArgumentException("endLine is required")
        val methodName = arguments["methodName"] as? String
            ?: throw IllegalArgumentException("methodName is required")
        val startColumn = (arguments["startColumn"] as? Number)?.toInt()
        val endColumn = (arguments["endColumn"] as? Number)?.toInt()
        val visibility = arguments["visibility"] as? String ?: "private"
        
        val project = ProjectManager.getInstance().openProjects.firstOrNull { it.name == projectName }
            ?: throw IllegalArgumentException("Project not found: $projectName")
        
        val request = ExtractMethodRequest(
            filePath = filePath,
            startLine = startLine,
            endLine = endLine,
            startColumn = startColumn,
            endColumn = endColumn,
            methodName = methodName,
            visibility = visibility
        )
        
        val result = RefactoringService.extractMethod(project, request)
        
        if (!result.success) {
            throw RuntimeException(result.error?.message ?: "Extract method failed")
        }
        
        return result
    }
}

