package io.miiken.intellijcontrolserver.models

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request to execute extract method refactoring (Phase 2 of two-phase API).
 * 
 * This combines the code selection information with the user's choices about
 * how to perform the extraction.
 */
@Schema(description = "Request to execute extract method refactoring with user-provided options")
data class ExtractMethodExecuteRequest(
    @Schema(
        description = "Path to the file containing the code to extract",
        example = "src/main/kotlin/com/example/Service.kt",
        required = true
    )
    val filePath: String,
    
    @Schema(
        description = "Line number where the code selection starts (1-based)",
        example = "10",
        required = true
    )
    val startLine: Int,
    
    @Schema(
        description = "Line number where the code selection ends (1-based)",
        example = "15",
        required = true
    )
    val endLine: Int,
    
    @Schema(
        description = "Column number where the selection starts on startLine (0-based, optional)",
        example = "4",
        required = false
    )
    val startColumn: Int? = null,
    
    @Schema(
        description = "Column number where the selection ends on endLine (0-based, optional)",
        example = "20",
        required = false
    )
    val endColumn: Int? = null,
    
    @Schema(
        description = "Name for the extracted method",
        example = "calculateTotal",
        required = true
    )
    val methodName: String,
    
    @Schema(
        description = "Order of parameters in the extracted method (optional)",
        example = "[\"userId\", \"amount\"]",
        required = false
    )
    val parameterOrder: List<String>? = null,
    
    @Schema(
        description = "Visibility modifier for the extracted method",
        example = "private",
        required = false
    )
    val visibility: String = "private",
    
    @Schema(
        description = "Whether the extracted method should be static",
        example = "false",
        required = false
    )
    val isStatic: Boolean = false,
    
    @Schema(
        description = "Optional explicit return type (if omitted, uses inferred type)",
        example = "String",
        required = false
    )
    val returnType: String? = null
)
