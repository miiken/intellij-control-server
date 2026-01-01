package io.miiken.intellijcontrolserver.server.routing

import java.lang.reflect.Method

data class Route(
    val method: String,
    val path: String,
    val controllerInstance: Any,
    val handlerMethod: Method,
    val operation: OperationMetadata? = null,
    val requestBodyType: Class<*>? = null
)

data class OperationMetadata(
    val summary: String,
    val description: String,
    val tags: List<String>,
    val responses: List<ResponseMetadata>
)

data class ResponseMetadata(
    val code: String,
    val description: String
)

