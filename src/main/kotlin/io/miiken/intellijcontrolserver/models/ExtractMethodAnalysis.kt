package io.miiken.intellijcontrolserver.models

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Result of analyzing a code selection for extract method refactoring.
 * 
 * Contains suggestions from IntelliJ's refactoring engine about how the
 * extraction should be performed.
 */
@Schema(description = "Analysis result for extract method refactoring")
data class ExtractMethodAnalysis(
    @Schema(
        description = "Whether the selected code can be extracted into a method",
        example = "true",
        required = true
    )
    val canExtract: Boolean,
    
    @Schema(
        description = "Suggested name for the extracted method based on code analysis",
        example = "calculateTotal",
        required = false
    )
    val suggestedMethodName: String? = null,
    
    @Schema(
        description = "Parameters detected in the selected code that need to be passed to the extracted method",
        required = true
    )
    val detectedParameters: List<MethodParameter> = emptyList(),
    
    @Schema(
        description = "Inferred return type of the extracted method",
        example = "String",
        required = false
    )
    val returnType: String? = null,
    
    @Schema(
        description = "Suggested visibility modifier (private, protected, public, internal)",
        example = "private",
        required = true
    )
    val suggestedVisibility: String = "private",
    
    @Schema(
        description = "Programming language of the analyzed code",
        example = "Kotlin",
        required = true
    )
    val language: String,
    
    @Schema(
        description = "Error message if extraction is not possible",
        example = "Selected code contains syntax errors",
        required = false
    )
    val errorMessage: String? = null
)

/**
 * Information about a method parameter detected during analysis.
 */
@Schema(description = "A parameter detected in the code to be extracted")
data class MethodParameter(
    @Schema(
        description = "Parameter name",
        example = "userId",
        required = true
    )
    val name: String,
    
    @Schema(
        description = "Parameter type",
        example = "String",
        required = true
    )
    val type: String,
    
    @Schema(
        description = "Whether this parameter is used as an output (modified in the extracted code)",
        example = "false",
        required = true
    )
    val isOutput: Boolean = false
)
