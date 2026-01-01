package io.miiken.intellijcontrolserver.server.controllers

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import io.miiken.intellijcontrolserver.models.ExtractMethodRequest
import io.miiken.intellijcontrolserver.models.RefactoringResult
import io.miiken.intellijcontrolserver.models.RenameRequest
import io.miiken.intellijcontrolserver.services.RefactoringService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/{projectName}/refactor")
@Tag(name = "Refactoring", description = "Code refactoring operations (rename, extract method, etc.)")
class RefactoringController {
    
    @POST
    @Path("/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Rename symbol",
        description = "Rename a class, method, variable, or parameter across all usages in the project. Verifies the oldName matches before renaming to prevent accidental changes."
    )
    @SwaggerRequestBody(
        description = "Rename request parameters",
        required = true,
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = RenameRequest::class),
            examples = [ExampleObject(
                name = "Rename method",
                value = """{"filePath":"src/main/kotlin/Service.kt","line":15,"oldName":"processUser","newName":"handleUser"}"""
            )]
        )]
    )
    @ApiResponse(
        responseCode = "200",
        description = "Rename successful",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = RefactoringResult::class),
            examples = [ExampleObject(
                value = """{"success":true,"filesChanged":["src/main/kotlin/Service.kt","src/test/kotlin/ServiceTest.kt"],"changesCount":12}"""
            )]
        )]
    )
    @ApiResponse(responseCode = "400", description = "Rename failed (conflict or invalid input)")
    @ApiResponse(responseCode = "404", description = "Project not found")
    fun rename(
        @PathParam("projectName")
        @Parameter(description = "Name of the project to perform refactoring in", required = true)
        projectName: String,
        request: RenameRequest
    ): RefactoringResult {
        validateRenameRequest(request)
        
        val project = findProject(projectName)
        val result = RefactoringService.rename(project, request)
        
        if (!result.success) {
            val error = result.error!!
            throw RefactoringException(error.code, error.message, error.details)
        }
        
        return result
    }
    
    @POST
    @Path("/extract-method")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Extract method",
        description = "Extract selected code into a new method. Automatically detects parameters and return type. Optionally specify parameter order and visibility."
    )
    @SwaggerRequestBody(
        description = "Extract method request parameters",
        required = true,
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ExtractMethodRequest::class),
            examples = [ExampleObject(
                name = "Basic extraction",
                value = """{"filePath":"src/main/kotlin/Service.kt","startLine":10,"endLine":15,"methodName":"calculateTotal"}"""
            ), ExampleObject(
                name = "With parameter order and columns",
                value = """{"filePath":"src/main/kotlin/Service.kt","startLine":10,"endLine":15,"startColumn":4,"endColumn":20,"methodName":"calculateTotal","parameterOrder":["userId","amount","currency"],"visibility":"private"}"""
            )]
        )]
    )
    @ApiResponse(
        responseCode = "200",
        description = "Extract method successful",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = RefactoringResult::class),
            examples = [ExampleObject(
                value = """{"success":true,"filesChanged":["src/main/kotlin/Service.kt"],"changesCount":1}"""
            )]
        )]
    )
    @ApiResponse(responseCode = "400", description = "Extract method failed")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @ApiResponse(responseCode = "501", description = "Not yet implemented")
    fun extractMethod(
        @PathParam("projectName")
        @Parameter(description = "Name of the project to perform refactoring in", required = true)
        projectName: String,
        request: ExtractMethodRequest
    ): RefactoringResult {
        validateExtractMethodRequest(request)
        
        val project = findProject(projectName)
        val result = RefactoringService.extractMethod(project, request)
        
        if (!result.success) {
            val error = result.error!!
            val statusCode = if (error.code == "NOT_IMPLEMENTED") 501 else 400
            throw RefactoringException(error.code, error.message, error.details, statusCode)
        }
        
        return result
    }
    
    private fun findProject(projectName: String): Project {
        val projects = ProjectManager.getInstance().openProjects
        return projects.firstOrNull { it.name == projectName }
            ?: throw ProjectNotFoundException(projectName)
    }
    
    internal fun validateRenameRequest(request: RenameRequest) {
        if (request.filePath.isBlank()) {
            throw RefactoringException("INVALID_REQUEST", "filePath is required")
        }
        if (request.line < 1) {
            throw RefactoringException("INVALID_REQUEST", "line must be positive (1-based)")
        }
        if (request.oldName.isBlank()) {
            throw RefactoringException("INVALID_REQUEST", "oldName is required")
        }
        if (request.newName.isBlank()) {
            throw RefactoringException("INVALID_REQUEST", "newName is required")
        }
        if (request.oldName == request.newName) {
            throw RefactoringException("INVALID_REQUEST", "oldName and newName cannot be the same")
        }
    }
    
    internal fun validateExtractMethodRequest(request: ExtractMethodRequest) {
        if (request.filePath.isBlank()) {
            throw RefactoringException("INVALID_REQUEST", "filePath is required")
        }
        if (request.startLine < 1) {
            throw RefactoringException("INVALID_REQUEST", "startLine must be positive (1-based)")
        }
        if (request.endLine < 1) {
            throw RefactoringException("INVALID_REQUEST", "endLine must be positive (1-based)")
        }
        if (request.endLine < request.startLine) {
            throw RefactoringException("INVALID_REQUEST", "endLine must be greater than or equal to startLine")
        }
        if (request.startColumn != null && request.startColumn < 0) {
            throw RefactoringException("INVALID_REQUEST", "startColumn must be non-negative (0-based)")
        }
        if (request.endColumn != null && request.endColumn < 0) {
            throw RefactoringException("INVALID_REQUEST", "endColumn must be non-negative (0-based)")
        }
        if (request.methodName.isBlank()) {
            throw RefactoringException("INVALID_REQUEST", "methodName is required")
        }
        if (!request.methodName.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))) {
            throw RefactoringException("INVALID_REQUEST", "methodName must be a valid identifier")
        }
        if (request.visibility !in listOf("private", "protected", "public", "internal")) {
            throw RefactoringException("INVALID_REQUEST", "visibility must be one of: private, protected, public, internal")
        }
    }
    
    class RefactoringException(
        val code: String,
        override val message: String,
        val details: Map<String, Any>? = null,
        val statusCode: Int = 400
    ) : RuntimeException(message)
    
    class ProjectNotFoundException(projectName: String) : RuntimeException("Project '$projectName' not found")
}

