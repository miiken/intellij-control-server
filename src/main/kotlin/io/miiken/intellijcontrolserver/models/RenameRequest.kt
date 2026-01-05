package io.miiken.intellijcontrolserver.models

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to rename a symbol (class, method, variable, parameter)")
data class RenameRequest(
    @Schema(
        description = "Name of the IntelliJ project (required for MCP, provided via URL path for HTTP API)",
        example = "my-project",
        required = false
    )
    val projectName: String? = null,
    
    @Schema(
        description = "Path to the file containing the element to rename (relative to project root)",
        example = "src/main/kotlin/com/example/Service.kt",
        required = true
    )
    val filePath: String,
    
    @Schema(
        description = "Line number where the element is located (1-based, as shown in editors)",
        example = "15",
        required = true
    )
    val line: Int,
    
    @Schema(
        description = "Current name of the element to rename. Used to locate and verify the correct element on the line.",
        example = "processUser",
        required = true
    )
    val oldName: String,
    
    @Schema(
        description = "New name for the element",
        example = "handleUser",
        required = true
    )
    val newName: String,
    
    @Schema(
        description = "Whether to update occurrences in string literals (e.g., logging: logger.info(\"methodName | ...\")). Defaults to true for methods/functions only.",
        example = "true",
        required = false
    )
    val searchInStrings: Boolean? = null
)

