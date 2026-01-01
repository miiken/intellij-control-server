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
        description = "Line number where the code selection starts (1-based, as shown in editors)",
        example = "10",
        required = true
    )
    val startLine: Int,
    
    @Schema(
        description = "Line number where the code selection ends (1-based, as shown in editors)",
        example = "15",
        required = true
    )
    val endLine: Int,
    
    @Schema(
        description = "Column number where the selection starts on startLine (0-based). If omitted, uses start of line.",
        example = "4",
        required = false
    )
    val startColumn: Int? = null,
    
    @Schema(
        description = "Column number where the selection ends on endLine (0-based). If omitted, uses end of line.",
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

