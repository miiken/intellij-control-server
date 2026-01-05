package io.miiken.intellijcontrolserver.mcp

import com.intellij.openapi.diagnostic.Logger

/**
 * Registry for MCP tools
 * 
 * Tools self-register by calling register() in their companion object init block.
 * To add a new tool: create a class implementing Tool<IN, OUT> with self-registration.
 */
object ToolRegistry {
    private val logger = Logger.getInstance(ToolRegistry::class.java)
    private val tools = mutableMapOf<String, Tool<*, *>>()
    
    init {
        loadAllTools()
    }
    
    /**
     * Register a tool (called by tools during class initialization)
     */
    fun register(tool: Tool<*, *>) {
        if (tools.containsKey(tool.name)) {
            logger.warn("Tool ${tool.name} already registered, skipping")
            return
        }
        
        tools[tool.name] = tool
        logger.info("Registered MCP tool: ${tool.name}")
    }
    
    fun getAllTools(): List<Tool<*, *>> = tools.values.toList()
    
    fun getTool(name: String): Tool<*, *>? = tools[name]
    
    fun getToolCount(): Int = tools.size
    
    /**
     * Trigger loading of all tool classes
     * This ensures their companion object init blocks run and they self-register
     */
    private fun loadAllTools() {
        val toolClasses = findToolClasses()
        
        logger.info("Attempting to load ${toolClasses.size} tool classes")
        
        toolClasses.forEach { className ->
            try {
                Class.forName(className, true, ToolRegistry::class.java.classLoader)
                logger.info("Successfully loaded tool class: $className")
            } catch (e: Exception) {
                logger.error("Failed to load tool class: $className", e)
            }
        }
        
        logger.info("Tool registry initialized with ${tools.size} tools")
    }
    
    /**
     * Find all tool classes in the tools package
     * Uses classpath scanning to discover Tool implementations
     */
    private fun findToolClasses(): List<String> {
        val toolPackage = "io.miiken.intellijcontrolserver.mcp.tools"
        val toolClasses = mutableListOf<String>()
        
        try {
            // Use this class's classloader (the plugin classloader) instead of thread context
            val classLoader = ToolRegistry::class.java.classLoader
            val packagePath = toolPackage.replace('.', '/')
            val resources = classLoader.getResources(packagePath)
            
            logger.info("Scanning for tools in package: $toolPackage using classLoader: ${classLoader.javaClass.name}")
            
            while (resources.hasMoreElements()) {
                val resource = resources.nextElement()
                val protocol = resource.protocol
                
                logger.info("Found resource: $resource with protocol: $protocol")
                
                if (protocol == "file" || protocol == "jar") {
                    val classes = findClassesInResource(resource, toolPackage)
                    logger.info("Found ${classes.size} classes in resource: $resource")
                    toolClasses.addAll(classes)
                }
            }
            
            logger.info("Total tool classes found: ${toolClasses.size}, classes: $toolClasses")
        } catch (e: Exception) {
            logger.error("Failed to scan for tool classes", e)
        }
        
        return toolClasses
    }
    
    private fun findClassesInResource(resource: java.net.URL, packageName: String): List<String> {
        val classes = mutableListOf<String>()
        
        try {
            when (resource.protocol) {
                "file" -> {
                    val directory = java.io.File(resource.toURI())
                    if (directory.exists() && directory.isDirectory) {
                        directory.listFiles()?.forEach { file ->
                            if (file.name.endsWith(".class")) {
                                val className = file.name.removeSuffix(".class")
                                classes.add("$packageName.$className")
                            }
                        }
                    }
                }
                "jar" -> {
                    val jarPath = resource.path.substringAfter("file:").substringBefore("!")
                    val jarFile = java.util.jar.JarFile(jarPath)
                    val packagePath = packageName.replace('.', '/')
                    
                    jarFile.entries().asIterator().forEach { entry ->
                        val name = entry.name
                        if (name.startsWith(packagePath) && name.endsWith(".class")) {
                            val className = name.removeSuffix(".class").replace('/', '.')
                            if (className.startsWith(packageName) && !className.contains('$')) {
                                classes.add(className)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to scan resource: $resource", e)
        }
        
        return classes
    }
}

