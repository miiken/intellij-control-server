package io.miiken.intellijcontrolserver.server.routing

import com.intellij.openapi.diagnostic.Logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import java.lang.reflect.Method

class ControllerRegistry {
    private val logger = Logger.getInstance(ControllerRegistry::class.java)
    private val routes = mutableListOf<Route>()
    
    fun registerController(controller: Any) {
        val controllerClass = controller::class.java
        val classPath = controllerClass.getAnnotation(Path::class.java)
        
        if (classPath == null) {
            logger.warn("Class ${controllerClass.simpleName} is not annotated with @Path")
            return
        }
        
        val basePath = classPath.value.trimEnd('/')
        val classTag = controllerClass.getAnnotation(io.swagger.v3.oas.annotations.tags.Tag::class.java)
        
        controllerClass.declaredMethods.forEach { method ->
            registerMethodIfMapped(controller, basePath, method, classTag)
        }
    }
    
    private fun registerMethodIfMapped(controller: Any, basePath: String, method: Method, classTag: io.swagger.v3.oas.annotations.tags.Tag?) {
        val methodPath = method.getAnnotation(Path::class.java)?.value ?: ""
        val httpMethod = when {
            method.isAnnotationPresent(GET::class.java) -> "GET"
            method.isAnnotationPresent(POST::class.java) -> "POST"
            else -> null
        }
        
        if (httpMethod != null) {
            registerRoute(httpMethod, basePath, methodPath, controller, method, classTag)
        }
    }
    
    private fun registerRoute(
        httpMethod: String,
        basePath: String,
        methodPath: String,
        controller: Any,
        method: Method,
        classTag: io.swagger.v3.oas.annotations.tags.Tag?
    ) {
        val fullPath = buildPath(basePath, methodPath)
        val metadata = extractOperationMetadata(method, classTag)
        val requestBodyType = extractRequestBodyType(method)
        
        val route = Route(
            method = httpMethod,
            path = fullPath,
            controllerInstance = controller,
            handlerMethod = method,
            operation = metadata,
            requestBodyType = requestBodyType
        )
        
        routes.add(route)
        logger.info("Registered $httpMethod $fullPath -> ${controller::class.simpleName}.${method.name}")
    }
    
    private fun buildPath(basePath: String, methodPath: String): String {
        val cleanBase = basePath.trimEnd('/')
        val cleanMethod = methodPath.trimStart('/')
        return if (cleanMethod.isEmpty()) cleanBase else "$cleanBase/$cleanMethod"
    }
    
    private fun extractOperationMetadata(method: Method, classTag: io.swagger.v3.oas.annotations.tags.Tag?): OperationMetadata? {
        val operation = method.getAnnotation(Operation::class.java) ?: return null
        val responses = method.getAnnotationsByType(ApiResponse::class.java)
        
        val tags = mutableListOf<String>()
        if (operation.tags.isNotEmpty()) {
            tags.addAll(operation.tags)
        } else if (classTag != null) {
            tags.add(classTag.name)
        }
        
        return OperationMetadata(
            summary = operation.summary,
            description = operation.description,
            tags = tags,
            responses = responses.map { ResponseMetadata(it.responseCode, it.description) }
        )
    }
    
    fun getRoutes(): List<Route> = routes.toList()
    
    fun findRoute(method: String, path: String): Route? {
        return routes.firstOrNull { route ->
            route.method == method && matchesPath(route.path, path)
        }
    }
    
    private fun extractRequestBodyType(method: Method): Class<*>? {
        return method.parameters.firstOrNull { param ->
            !param.isAnnotationPresent(jakarta.ws.rs.PathParam::class.java) &&
            param.type != com.sun.net.httpserver.HttpExchange::class.java &&
            param.type.packageName?.startsWith("io.miiken") == true
        }?.type
    }
    
    private fun matchesPath(routePath: String, requestPath: String): Boolean {
        val routeParts = routePath.split("/").filter { it.isNotEmpty() }
        val requestParts = requestPath.split("/").filter { it.isNotEmpty() }
        
        if (routeParts.size != requestParts.size) return false
        
        return routeParts.indices.all { i ->
            val routePart = routeParts[i]
            routePart.startsWith("{") && routePart.endsWith("}") || routePart == requestParts[i]
        }
    }
}

