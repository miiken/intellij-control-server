package io.miiken.intellijcontrolserver.server.openapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.miiken.intellijcontrolserver.server.routing.ControllerRegistry
import io.miiken.intellijcontrolserver.server.routing.Route

class OpenApiGenerator(private val registry: ControllerRegistry) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    fun generateSpec(serverUrl: String = "http://127.0.0.1:8765"): String {
        val spec = mutableMapOf<String, Any>()
        
        spec["openapi"] = "3.0.3"
        spec["info"] = mapOf(
            "title" to "IntelliJ Control Server API",
            "description" to "HTTP API for programmatic control of IntelliJ IDEA operations. Automatically generated from annotations.",
            "version" to "1.0.0",
            "contact" to mapOf(
                "name" to "IntelliJ Control Server",
                "url" to "https://github.com/miiken/intellij-control-server"
            )
        )
        
        spec["servers"] = listOf(
            mapOf("url" to serverUrl, "description" to "Local IntelliJ instance")
        )
        
        spec["tags"] = extractTags()
        spec["paths"] = generatePaths()
        spec["components"] = generateComponents()
        
        return gson.toJson(spec)
    }
    
    private fun extractTags(): List<Map<String, Any>> {
        val tagsMap = mutableMapOf<String, String>()
        
        registry.getRoutes().forEach { route ->
            route.operation?.tags?.forEach { tagName ->
                if (!tagsMap.containsKey(tagName)) {
                    val tagAnnotation = findTagAnnotation(route)
                    tagsMap[tagName] = tagAnnotation?.description ?: ""
                }
            }
        }
        
        return tagsMap.map { (name, description) ->
            if (description.isNotEmpty()) {
                mapOf("name" to name, "description" to description)
            } else {
                mapOf("name" to name)
            }
        }
    }
    
    private fun findTagAnnotation(route: Route): io.swagger.v3.oas.annotations.tags.Tag? {
        return route.controllerInstance.javaClass.getAnnotation(io.swagger.v3.oas.annotations.tags.Tag::class.java)
    }
    
    private fun generatePaths(): Map<String, Any> {
        val paths = mutableMapOf<String, Any>()
        
        registry.getRoutes()
            .groupBy { it.path }
            .forEach { (path, routes) ->
                paths[path] = generatePathItem(routes)
            }
        
        return paths
    }
    
    private fun generatePathItem(routes: List<Route>): Map<String, Any> {
        val pathItem = mutableMapOf<String, Any>()
        
        routes.forEach { route ->
            val method = route.method.lowercase()
            pathItem[method] = generateOperation(route)
        }
        
        return pathItem
    }
    
    private fun generateOperation(route: Route): Map<String, Any> {
        val operation = mutableMapOf<String, Any>()
        val metadata = route.operation
        
        if (metadata != null) {
            operation["summary"] = metadata.summary
            operation["description"] = metadata.description
            operation["tags"] = metadata.tags
            operation["responses"] = generateResponses(metadata.responses.map { it.code to it.description })
        } else {
            operation["summary"] = "Operation at ${route.path}"
            operation["responses"] = generateResponses(listOf("200" to "Success"))
        }
        
        if (route.method == "POST") {
            operation["requestBody"] = mapOf(
                "required" to true,
                "content" to mapOf(
                    "application/json" to mapOf(
                        "schema" to mapOf("type" to "object")
                    )
                )
            )
        }
        
        return operation
    }
    
    private fun generateResponses(responses: List<Pair<String, String>>): Map<String, Any> {
        val responsesMap = mutableMapOf<String, Any>()
        
        responses.forEach { (code, description) ->
            responsesMap[code] = mapOf(
                "description" to description,
                "content" to mapOf(
                    "application/json" to mapOf(
                        "schema" to mapOf("type" to "object")
                    )
                )
            )
        }
        
        return responsesMap
    }
    
    private fun generateComponents(): Map<String, Any> {
        return mapOf(
            "schemas" to mapOf(
                "ErrorResponse" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "success" to mapOf("type" to "boolean"),
                        "error" to mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "code" to mapOf("type" to "string"),
                                "message" to mapOf("type" to "string"),
                                "details" to mapOf("type" to "object")
                            )
                        )
                    )
                )
            )
        )
    }
}

