package io.miiken.intellijcontrolserver.mcp

import com.googlecode.jsonrpc4j.JsonRpcBasicServer
import com.intellij.openapi.diagnostic.Logger
import java.io.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * MCP Server with stdio transport
 * 
 * Adapts jsonrpc4j's InputStream/OutputStream model to stdin/stdout
 * for MCP protocol communication.
 */
class McpStdioServer {
    private val logger = Logger.getInstance(McpStdioServer::class.java)
    private val running = AtomicBoolean(false)
    private var serverThread: Thread? = null
    
    private val service = McpServiceImpl()
    private val jsonRpcServer = JsonRpcBasicServer(service, McpService::class.java)
    
    fun start() {
        if (running.getAndSet(true)) {
            logger.warn("MCP server already running")
            return
        }
        
        logger.info("Starting MCP server on stdio")
        
        serverThread = Thread({
            try {
                runServerLoop()
            } catch (e: Exception) {
                logger.error("MCP server error", e)
            } finally {
                running.set(false)
            }
        }, "MCP-stdio-server").apply {
            isDaemon = true
            start()
        }
        
        logger.info("MCP server started")
    }
    
    fun stop() {
        if (!running.getAndSet(false)) {
            logger.warn("MCP server not running")
            return
        }
        
        serverThread?.interrupt()
        serverThread = null
        
        logger.info("MCP server stopped")
    }
    
    fun isRunning(): Boolean = running.get()
    
    private fun runServerLoop() {
        BufferedReader(InputStreamReader(System.`in`, Charsets.UTF_8)).use { reader ->
            PrintWriter(OutputStreamWriter(System.out, Charsets.UTF_8), true).use { writer ->
                
                while (running.get() && !Thread.currentThread().isInterrupted) {
                    try {
                        val line = reader.readLine() ?: break
                        
                        if (line.isBlank()) {
                            continue
                        }
                        
                        logger.debug("MCP received: $line")
                        
                        val response = processRequest(line)
                        
                        logger.debug("MCP sending: $response")
                        
                        writer.println(response)
                        writer.flush()
                        
                    } catch (e: InterruptedIOException) {
                        logger.info("MCP server interrupted")
                        break
                    } catch (e: IOException) {
                        if (running.get()) {
                            logger.error("MCP I/O error", e)
                        }
                        break
                    } catch (e: Exception) {
                        logger.error("MCP request processing error", e)
                        
                        val errorResponse = """{"jsonrpc":"2.0","error":{"code":-32603,"message":"Internal error: ${e.message}"},"id":null}"""
                        writer.println(errorResponse)
                        writer.flush()
                    }
                }
            }
        }
        
        logger.info("MCP server loop ended")
    }
    
    private fun processRequest(requestJson: String): String {
        val requestBytes = requestJson.toByteArray(Charsets.UTF_8)
        val inputStream = ByteArrayInputStream(requestBytes)
        val outputStream = ByteArrayOutputStream()
        
        jsonRpcServer.handleRequest(inputStream, outputStream)
        
        return String(outputStream.toByteArray(), Charsets.UTF_8).trim()
    }
}

