package io.miiken.intellijcontrolserver.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServerConfigTest {
    
    @Test
    fun `should create default config with valid values`() {
        val config = ServerConfig.DEFAULT
        
        assertEquals(8765, config.port)
        assertEquals("127.0.0.1", config.host)
        assertTrue(config.autoStart)
        assertEquals("INFO", config.logLevel)
        assertFalse(config.enableCors)
    }
    
    @Test
    fun `should validate port within valid range`() {
        val config = ServerConfig(port = 8080)
        
        val errors = config.validate()
        
        assertTrue(errors.isEmpty())
        assertTrue(config.isValid())
    }
    
    @Test
    fun `should reject port below minimum range`() {
        val config = ServerConfig(port = 1000)
        
        val errors = config.validate()
        
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("Port must be between") })
        assertFalse(config.isValid())
    }
    
    @Test
    fun `should reject port above maximum range`() {
        val config = ServerConfig(port = 70000)
        
        val errors = config.validate()
        
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("Port must be between") })
        assertFalse(config.isValid())
    }
    
    @Test
    fun `should accept port at minimum boundary`() {
        val config = ServerConfig(port = 1024)
        
        val errors = config.validate()
        
        assertTrue(errors.isEmpty())
    }
    
    @Test
    fun `should accept port at maximum boundary`() {
        val config = ServerConfig(port = 65535)
        
        val errors = config.validate()
        
        assertTrue(errors.isEmpty())
    }
    
    @Test
    fun `should reject blank host`() {
        val config = ServerConfig(host = "")
        
        val errors = config.validate()
        
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("Host cannot be blank") })
    }
    
    @Test
    fun `should accept valid log levels`() {
        val validLevels = listOf("DEBUG", "INFO", "WARN", "ERROR")
        
        validLevels.forEach { level ->
            val config = ServerConfig(logLevel = level)
            val errors = config.validate()
            assertTrue(errors.isEmpty(), "Log level $level should be valid")
        }
    }
    
    @Test
    fun `should reject invalid log level`() {
        val config = ServerConfig(logLevel = "INVALID")
        
        val errors = config.validate()
        
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("Log level must be") })
    }
    
    @Test
    fun `should collect multiple validation errors`() {
        val config = ServerConfig(
            port = 100,
            host = "",
            logLevel = "INVALID"
        )
        
        val errors = config.validate()
        
        assertEquals(3, errors.size)
    }
}

