package io.miiken.intellijcontrolserver.mcp

import kotlin.reflect.KClass

/**
 * Base interface for MCP tools with type-safe request/response
 * 
 * Implement this interface to create a new tool.
 * The tool will be automatically discovered and registered.
 * 
 * @param IN Input request type (e.g., ExtractMethodRequest)
 * @param OUT Output response type (e.g., RefactoringResult)
 */
interface Tool<IN : Any, OUT : Any> {
    /**
     * Unique tool name (e.g., "intellij_health_check")
     */
    val name: String
    
    /**
     * Human-readable description
     */
    val description: String
    
    /**
     * JSON Schema for input validation
     */
    val inputSchema: Map<String, Any>
    
    /**
     * Input request class for deserialization
     */
    val inputClass: KClass<IN>
    
    /**
     * Execute the tool with typed request
     * 
     * @param request Typed request object
     * @return Typed response object (will be JSON-serialized)
     * @throws IllegalArgumentException for invalid arguments
     * @throws RuntimeException for execution errors
     */
    fun execute(request: IN): OUT
}

