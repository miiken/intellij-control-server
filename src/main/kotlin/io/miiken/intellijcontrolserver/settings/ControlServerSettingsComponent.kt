package io.miiken.intellijcontrolserver.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

class ControlServerSettingsComponent {
    private val mainPanel: JPanel
    private val portField = JBTextField()
    private val hostField = JBTextField()
    private val autoStartCheckbox = JBCheckBox("Auto-start server when IntelliJ opens")
    private val logLevelComboBox = JComboBox(arrayOf("DEBUG", "INFO", "WARN", "ERROR"))
    private val enableCorsCheckbox = JBCheckBox("Enable CORS (allow cross-origin requests)")
    private val enableMcpCheckbox = JBCheckBox("Enable MCP (Model Context Protocol) server on stdio")
    
    init {
        portField.columns = 10
        hostField.columns = 20
        
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Port:"), portField, 1, false)
            .addTooltip("Port number for HTTP server (1024-65535). Default: 8765")
            .addLabeledComponent(JBLabel("Host:"), hostField, 1, false)
            .addTooltip("Host address to bind to. Use 127.0.0.1 for localhost only (recommended for security)")
            .addComponent(autoStartCheckbox, 1)
            .addLabeledComponent(JBLabel("Log Level:"), logLevelComboBox, 1, false)
            .addTooltip("Logging verbosity: DEBUG (verbose), INFO (normal), WARN (warnings only), ERROR (errors only)")
            .addComponent(enableCorsCheckbox, 1)
            .addTooltip("Allow requests from any origin. Enable only if you need to access from browser/other domains")
            .addComponent(enableMcpCheckbox, 1)
            .addTooltip("Enable MCP server for AI tool integration (e.g., Cursor). Uses stdio transport for JSON-RPC 2.0 communication")
            .addComponentFillVertically(JPanel(), 0)
            .panel
        
        mainPanel.border = JBUI.Borders.empty(10)
    }
    
    fun getPanel(): JComponent = mainPanel
    
    fun getPort(): Int = portField.text.toIntOrNull() ?: 8765
    
    fun setPort(port: Int) {
        portField.text = port.toString()
    }
    
    fun getHost(): String = hostField.text
    
    fun setHost(host: String) {
        hostField.text = host
    }
    
    fun getAutoStart(): Boolean = autoStartCheckbox.isSelected
    
    fun setAutoStart(autoStart: Boolean) {
        autoStartCheckbox.isSelected = autoStart
    }
    
    fun getLogLevel(): String = logLevelComboBox.selectedItem as String
    
    fun setLogLevel(logLevel: String) {
        logLevelComboBox.selectedItem = logLevel
    }
    
    fun getEnableCors(): Boolean = enableCorsCheckbox.isSelected
    
    fun setEnableCors(enableCors: Boolean) {
        enableCorsCheckbox.isSelected = enableCors
    }
    
    fun getEnableMcp(): Boolean = enableMcpCheckbox.isSelected
    
    fun setEnableMcp(enableMcp: Boolean) {
        enableMcpCheckbox.isSelected = enableMcp
    }
}

