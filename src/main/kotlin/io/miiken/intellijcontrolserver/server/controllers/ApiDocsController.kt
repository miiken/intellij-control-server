package io.miiken.intellijcontrolserver.server.controllers

import io.miiken.intellijcontrolserver.server.openapi.OpenApiGenerator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/")
@Tag(name = "Documentation", description = "API documentation endpoints")
class ApiDocsController(private val openApiGenerator: OpenApiGenerator) {
    
    @GET
    @Path("/openapi.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "OpenAPI specification",
        description = "Get the auto-generated OpenAPI 3.0 specification for this API"
    )
    @ApiResponse(responseCode = "200", description = "OpenAPI specification in JSON format")
    fun getOpenApiSpec(): String {
        return openApiGenerator.generateSpec()
    }
    
    @GET
    @Path("/api-docs")
    @Produces(MediaType.TEXT_HTML)
    @Operation(
        summary = "Swagger UI",
        description = "Interactive API documentation powered by Swagger UI"
    )
    @ApiResponse(responseCode = "200", description = "HTML page with Swagger UI")
    fun getSwaggerUI(): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>IntelliJ Control Server API</title>
                <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css" />
                <style>
                    html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
                    *, *:before, *:after { box-sizing: inherit; }
                    body { margin: 0; padding: 0; }
                </style>
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-standalone-preset.js"></script>
                <script>
                    window.onload = function() {
                        window.ui = SwaggerUIBundle({
                            url: "/openapi.json",
                            dom_id: '#swagger-ui',
                            deepLinking: true,
                            presets: [
                                SwaggerUIBundle.presets.apis,
                                SwaggerUIStandalonePreset
                            ],
                            plugins: [
                                SwaggerUIBundle.plugins.DownloadUrl
                            ],
                            layout: "StandaloneLayout"
                        });
                    };
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}

