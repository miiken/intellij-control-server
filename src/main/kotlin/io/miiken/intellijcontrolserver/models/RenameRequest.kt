package io.miiken.intellijcontrolserver.models

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to rename a symbol (class, method, variable, parameter)")
data class RenameRequest(
    @Schema(
        description = "Path to the file containing the element to rename (relative to project root)",
        example = "src/main/kotlin/com/example/Service.kt",
        required = true
    )
    val filePath: String,
    
    @Schema(
        description = "Character offset in the file where the element is located (0-based position)",
        example = "150",
        required = true
    )
    val offset: Int,
    
    @Schema(
        description = "Current name of the element. Used to verify the element at the offset matches expectations.",
        example = "Service",
        required = true
    )
    val oldName: String,
    
    @Schema(
        description = "New name for the element",
        example = "UserService",
        required = true
    )
    val newName: String,
    
    @Schema(
        description = "Whether to rename occurrences in comments",
        example = "false",
        required = false
    )
    val searchInComments: Boolean = false
)

