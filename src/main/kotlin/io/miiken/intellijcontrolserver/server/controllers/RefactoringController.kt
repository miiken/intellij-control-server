package io.miiken.intellijcontrolserver.server.controllers

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
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

/**
 * REST controller for refactoring operations.
 * Provides endpoints for symbol renaming and other refactorings.
 */
@Tag(name = "Refactoring", description = "Code refactoring operations")
@Path("/{projectName}/refactor")
class RefactoringController {
    
    @POST
    @Path("/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Rename a symbol",
        description = "Rename a variable, function, class, or other symbol at the specified location using IntelliJ's native refactoring engine"
    )
    @SwaggerRequestBody(
        description = "Rename request parameters",
        required = true,
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = RenameRequest::class),
            examples = [ExampleObject(
                value = """{"filePath":"src/main/kotlin/Service.kt","line":10,"column":5,"oldName":"userId","newName":"customerId"}"""
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
                value = """{"success":true,"filesChanged":["src/main/kotlin/Service.kt","src/main/kotlin/Controller.kt"],"changesCount":5}"""
            )]
        )]
    )
    @ApiResponse(responseCode = "400", description = "Rename failed")
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
    
    class RefactoringException(
        val code: String,
        override val message: String,
        val details: Map<String, Any>? = null,
        val statusCode: Int = 400
    ) : RuntimeException(message)
    
    class ProjectNotFoundException(projectName: String) : RuntimeException("Project '$projectName' not found")
}
