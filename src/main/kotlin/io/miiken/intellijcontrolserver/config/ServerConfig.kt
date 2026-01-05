package io.miiken.intellijcontrolserver.config

/**
 * Configuration for the Control Server
 */
data class ServerConfig(
    /**
     * Port number to bind the HTTP server to
     * Default: 8765
     */
    val port: Int = 8765,
    
    /**
     * Host address to bind to
     * Default: 127.0.0.1 (localhost only for security)
     */
    val host: String = "127.0.0.1",
    
    /**
     * Whether to start the server automatically when IntelliJ starts
     * Default: true
     */
    val autoStart: Boolean = true,
    
    /**
     * Log level: DEBUG, INFO, WARN, ERROR
     * Default: INFO
     */
    val logLevel: String = "INFO",
    
    /**
     * Enable CORS headers (not recommended for production)
     * Default: false
     */
    val enableCors: Boolean = false,
    
    /**
     * Enable MCP (Model Context Protocol) server on stdio
     * Default: true
     */
    val enableMcp: Boolean = true,
    
    /**
     * When renaming a method, also update string literals in the method body
     * Default: true
     */
    val renameStringsInMethodBody: Boolean = true,
    
    /**
     * When renaming a method, also update annotation properties attached to the method
     * Default: true
     */
    val renameInAnnotations: Boolean = true
) {
    companion object {
        /**
         * Default configuration
         */
        val DEFAULT = ServerConfig()
    }
    
    /**
     * Validate configuration values
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (port < 1024 || port > 65535) {
            errors.add("Port must be between 1024 and 65535")
        }
        
        if (host.isBlank()) {
            errors.add("Host cannot be blank")
        }
        
        if (logLevel !in listOf("DEBUG", "INFO", "WARN", "ERROR")) {
            errors.add("Log level must be DEBUG, INFO, WARN, or ERROR")
        }
        
        return errors
    }
    
    /**
     * Check if configuration is valid
     */
    fun isValid(): Boolean = validate().isEmpty()
}

