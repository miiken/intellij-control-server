package io.miiken.intellijcontrolserver.models

import io.swagger.v3.oas.annotations.media.Schema

/**
 * User-provided options for executing extract method refactoring.
 * 
 * These options are typically determined after the analyze phase,
 * where the AI/user can review suggestions and make decisions.
 */
@Schema(description = "Options for executing extract method refactoring")
data class ExtractMethodOptions(
    @Schema(
        description = "Name for the extracted method",
        example = "calculateTotal",
        required = true
    )
    val methodName: String,
    
    @Schema(
        description = "Order of parameters in the extracted method. If omitted, uses the order detected during analysis.",
        example = "[\"amount\", \"userId\"]",
        required = false
    )
    val parameterOrder: List<String>? = null,
    
    @Schema(
        description = "Visibility modifier for the extracted method (private, protected, public, internal)",
        example = "private",
        required = false
    )
    val visibility: String = "private",
    
    @Schema(
        description = "Whether the extracted method should be static (Java) or top-level (Kotlin)",
        example = "false",
        required = false
    )
    val isStatic: Boolean = false,
    
    @Schema(
        description = "Optional explicit return type. If omitted, uses the type inferred during analysis.",
        example = "String",
        required = false
    )
    val returnType: String? = null
)
