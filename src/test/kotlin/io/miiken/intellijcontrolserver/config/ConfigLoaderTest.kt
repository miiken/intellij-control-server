package io.miiken.intellijcontrolserver.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigLoaderTest {
    
    @TempDir
    lateinit var tempDir: File
    
    @Test
    fun `should return default config when file does not exist`() {
        val nonExistentPath = "${tempDir.absolutePath}/nonexistent.json"
        
        val result = ConfigLoader.load(nonExistentPath)
        
        assertEquals(ServerConfig.DEFAULT, result)
    }
    
    @Test
    fun `should load valid config from file`() {
        val configFile = File(tempDir, "config.json")
        configFile.writeText("""
            {
                "port": 9000,
                "host": "localhost",
                "autoStart": false,
                "logLevel": "DEBUG",
                "enableCors": true
            }
        """.trimIndent())
        
        val result = ConfigLoader.load(configFile.absolutePath)
        
        assertEquals(9000, result.port)
        assertEquals("localhost", result.host)
        assertFalse(result.autoStart)
        assertEquals("DEBUG", result.logLevel)
        assertTrue(result.enableCors)
    }
    
    @Test
    fun `should return default config when JSON is malformed`() {
        val configFile = File(tempDir, "bad-config.json")
        configFile.writeText("{ invalid json }")
        
        val result = ConfigLoader.load(configFile.absolutePath)
        
        assertEquals(ServerConfig.DEFAULT, result)
    }
    
    @Test
    fun `should return default config when config validation fails`() {
        val configFile = File(tempDir, "invalid-config.json")
        configFile.writeText("""
            {
                "port": 100,
                "host": "127.0.0.1",
                "autoStart": true,
                "logLevel": "INFO",
                "enableCors": false
            }
        """.trimIndent())
        
        val result = ConfigLoader.load(configFile.absolutePath)
        
        assertEquals(ServerConfig.DEFAULT, result)
    }
    
    @Test
    fun `should save config to file successfully`() {
        val configFile = File(tempDir, "save-test.json")
        val config = ServerConfig(
            port = 9000,
            host = "localhost",
            autoStart = false,
            logLevel = "DEBUG",
            enableCors = true
        )
        
        val result = ConfigLoader.save(config, configFile.absolutePath)
        
        assertTrue(result)
        assertTrue(configFile.exists())
    }
    
    @Test
    fun `should create parent directories when saving config`() {
        val nestedPath = File(tempDir, "nested/deep/config.json")
        val config = ServerConfig.DEFAULT
        
        val result = ConfigLoader.save(config, nestedPath.absolutePath)
        
        assertTrue(result)
        assertTrue(nestedPath.exists())
        assertTrue(nestedPath.parentFile.exists())
    }
    
    @Test
    fun `should save and load config correctly`() {
        val configFile = File(tempDir, "roundtrip.json")
        val originalConfig = ServerConfig(
            port = 7777,
            host = "0.0.0.0",
            autoStart = false,
            logLevel = "WARN",
            enableCors = true
        )
        
        ConfigLoader.save(originalConfig, configFile.absolutePath)
        val loadedConfig = ConfigLoader.load(configFile.absolutePath)
        
        assertEquals(originalConfig, loadedConfig)
    }
    
    @Test
    fun `should handle file with only partial config`() {
        val configFile = File(tempDir, "partial-config.json")
        configFile.writeText("""
            {
                "port": 9000
            }
        """.trimIndent())
        
        val result = ConfigLoader.load(configFile.absolutePath)
        
        assertEquals(9000, result.port)
    }
}

