package io.hibob.intellijcontrolserver.config

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Loads server configuration from file or uses defaults
 */
object ConfigLoader {
    private val logger = Logger.getInstance(ConfigLoader::class.java)
    private val gson = Gson()
    
    /**
     * Default config file location
     */
    private val DEFAULT_CONFIG_PATH = "${System.getProperty("user.home")}/.intellij-control-server/config.json"
    
    /**
     * Load configuration from default location or use defaults
     */
    fun load(): ServerConfig {
        return load(DEFAULT_CONFIG_PATH)
    }
    
    /**
     * Load configuration from specific path
     * 
     * @param path Path to config file
     * @return Loaded configuration or default if file doesn't exist/is invalid
     */
    fun load(path: String): ServerConfig {
        val file = File(path)
        
        if (!file.exists()) {
            logger.info("Config file not found at $path, using default configuration")
            return ServerConfig.DEFAULT
        }
        
        return try {
            val json = file.readText()
            val config = gson.fromJson(json, ServerConfig::class.java)
            
            // Validate configuration
            val errors = config.validate()
            if (errors.isNotEmpty()) {
                logger.warn("Invalid configuration: ${errors.joinToString(", ")}. Using default configuration.")
                ServerConfig.DEFAULT
            } else {
                logger.info("Loaded configuration from $path")
                config
            }
        } catch (e: JsonSyntaxException) {
            logger.error("Failed to parse config file at $path: ${e.message}. Using default configuration.", e)
            ServerConfig.DEFAULT
        } catch (e: Exception) {
            logger.error("Failed to load config file at $path: ${e.message}. Using default configuration.", e)
            ServerConfig.DEFAULT
        }
    }
    
    /**
     * Save configuration to default location
     * 
     * @param config Configuration to save
     * @return true if saved successfully, false otherwise
     */
    fun save(config: ServerConfig): Boolean {
        return save(config, DEFAULT_CONFIG_PATH)
    }
    
    /**
     * Save configuration to specific path
     * 
     * @param config Configuration to save
     * @param path Path to save config file
     * @return true if saved successfully, false otherwise
     */
    fun save(config: ServerConfig, path: String): Boolean {
        return try {
            val file = File(path)
            
            // Create parent directories if they don't exist
            file.parentFile?.mkdirs()
            
            val json = gson.toJson(config)
            file.writeText(json)
            
            logger.info("Saved configuration to $path")
            true
        } catch (e: Exception) {
            logger.error("Failed to save config file at $path: ${e.message}", e)
            false
        }
    }
    
    /**
     * Create example config file at default location
     */
    fun createExampleConfig(): Boolean {
        val exampleConfig = ServerConfig.DEFAULT
        return save(exampleConfig)
    }
}

