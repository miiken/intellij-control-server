package io.miiken.intellijcontrolserver.models

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to extract a code block into a new method")
data class ExtractMethodRequest(
    @Schema(
        description = "Path to the file containing the code to extract",
        example = "src/main/kotlin/com/example/Service.kt",
        required = true
    )
    val filePath: String,
    
    @Schema(
        description = "Character offset where the code selection starts",
        example = "200",
        required = true
    )
    val startOffset: Int,
    
    @Schema(
        description = "Character offset where the code selection ends",
        example = "350",
        required = true
    )
    val endOffset: Int,
    
    @Schema(
        description = "Name for the extracted method",
        example = "calculateTotal",
        required = true
    )
    val methodName: String,
    
    @Schema(
        description = "Optional order of parameters in the extracted method. If omitted, IntelliJ uses smart defaults.",
        example = "[\"userId\", \"amount\", \"currency\"]",
        required = false
    )
    val parameterOrder: List<String>? = null,
    
    @Schema(
        description = "Visibility modifier for the extracted method",
        example = "private",
        required = false
    )
    val visibility: String = "private"
)

