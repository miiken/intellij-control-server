package io.miiken.intellijcontrolserver.models

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Result of a refactoring operation")
data class RefactoringResult(
    @Schema(
        description = "Whether the refactoring was successful",
        example = "true",
        required = true
    )
    val success: Boolean,
    
    @Schema(
        description = "List of files that were modified by the refactoring",
        example = "[\"src/main/kotlin/Service.kt\", \"src/test/kotlin/ServiceTest.kt\"]",
        required = false
    )
    val filesChanged: List<String> = emptyList(),
    
    @Schema(
        description = "Total number of changes made across all files",
        example = "12",
        required = false
    )
    val changesCount: Int = 0,
    
    @Schema(
        description = "Error details if the refactoring failed",
        required = false
    )
    val error: RefactoringError? = null
)

@Schema(description = "Error details for a failed refactoring operation")
data class RefactoringError(
    @Schema(
        description = "Error code for programmatic handling",
        example = "NAME_COLLISION",
        required = true
    )
    val code: String,
    
    @Schema(
        description = "Human-readable error message",
        example = "Name 'UserService' already exists",
        required = true
    )
    val message: String,
    
    @Schema(
        description = "Additional error details (structure varies by error type)",
        required = false
    )
    val details: Map<String, Any>? = null
)

