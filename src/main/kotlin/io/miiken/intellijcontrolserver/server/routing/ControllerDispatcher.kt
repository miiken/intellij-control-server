package io.miiken.intellijcontrolserver.server.routing

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.miiken.intellijcontrolserver.server.ResponseBuilder
import java.lang.reflect.Parameter

class ControllerDispatcher(private val registry: ControllerRegistry) : HttpHandler {
    private val logger = Logger.getInstance(ControllerDispatcher::class.java)
    private val gson = Gson()
    
    override fun handle(exchange: HttpExchange) {
        val method = exchange.requestMethod
        val path = exchange.requestURI.path
        
        logger.info("Incoming request: $method $path")
        
        val route = registry.findRoute(method, path)
        if (route == null) {
            ResponseBuilder.sendError(exchange, "NOT_FOUND", "No route found for $method $path", statusCode = 404)
            return
        }
        
        try {
            val result = invokeController(route, exchange)
            handleResult(result, exchange, route)
        } catch (e: IllegalArgumentException) {
            ResponseBuilder.sendError(exchange, "INVALID_REQUEST", e.message ?: "Invalid request", statusCode = 400)
        } catch (e: Exception) {
            when (e.javaClass.simpleName) {
                "RefactoringException" -> handleRefactoringException(e, exchange)
                "ProjectNotFoundException" -> ResponseBuilder.sendError(
                    exchange,
                    "PROJECT_NOT_FOUND",
                    e.message ?: "Project not found",
                    statusCode = 404
                )
                else -> {
                    logger.error("Error dispatching to controller", e)
                    ResponseBuilder.sendError(
                        exchange,
                        "INTERNAL_ERROR",
                        "Internal server error: ${e.message}",
                        statusCode = 500
                    )
                }
            }
        }
    }
    
    private fun invokeController(route: Route, exchange: HttpExchange): Any? {
        val pathParams = extractPathParameters(route.path, exchange.requestURI.path)
        val args = resolveMethodArguments(route.handlerMethod.parameters, exchange, route.method, pathParams)
        return route.handlerMethod.invoke(route.controllerInstance, *args.toTypedArray())
    }
    
    private fun extractPathParameters(routePath: String, requestPath: String): Map<String, String> {
        val routeParts = routePath.split("/").filter { it.isNotEmpty() }
        val requestParts = requestPath.split("/").filter { it.isNotEmpty() }
        
        val params = mutableMapOf<String, String>()
        routeParts.forEachIndexed { index, part ->
            if (part.startsWith("{") && part.endsWith("}") && index < requestParts.size) {
                val paramName = part.substring(1, part.length - 1)
                params[paramName] = requestParts[index]
            }
        }
        return params
    }
    
    private fun resolveMethodArguments(
        parameters: Array<Parameter>,
        exchange: HttpExchange,
        httpMethod: String,
        pathParams: Map<String, String>
    ): List<Any?> {
        if (parameters.isEmpty()) return emptyList()
        
        return parameters.map { param ->
            val pathParam = param.getAnnotation(jakarta.ws.rs.PathParam::class.java)
            when {
                pathParam != null -> pathParams[pathParam.value]
                param.type == HttpExchange::class.java -> exchange
                httpMethod == "POST" && param.type != HttpExchange::class.java -> {
                    val body = exchange.requestBody.bufferedReader().use { it.readText() }
                    gson.fromJson(body, param.type)
                }
                else -> null
            }
        }
    }
    
    private fun handleResult(result: Any?, exchange: HttpExchange, route: Route) {
        val produces = route.handlerMethod.getAnnotation(jakarta.ws.rs.Produces::class.java)
        val contentType = produces?.value?.firstOrNull() ?: "application/json"
        
        when {
            result == null -> ResponseBuilder.sendSuccess(exchange, mapOf<String, Any>())
            result is String && contentType == "text/html" -> {
                exchange.responseHeaders.set("Content-Type", "text/html; charset=UTF-8")
                val bytes = result.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            result is String -> ResponseBuilder.sendText(exchange, result)
            result is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                ResponseBuilder.sendSuccess(exchange, result as Map<String, Any>)
            }
            else -> ResponseBuilder.sendSuccess(exchange, result)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun handleRefactoringException(e: Exception, exchange: HttpExchange) {
        val codeField = e.javaClass.getDeclaredField("code").apply { isAccessible = true }
        val detailsField = e.javaClass.getDeclaredField("details").apply { isAccessible = true }
        val statusCodeField = e.javaClass.getDeclaredField("statusCode").apply { isAccessible = true }
        
        val code = codeField.get(e) as String
        val details = detailsField.get(e) as? Map<String, Any>
        val statusCode = statusCodeField.get(e) as Int
        
        ResponseBuilder.sendError(exchange, code, e.message ?: "Refactoring error", details, statusCode)
    }
}

