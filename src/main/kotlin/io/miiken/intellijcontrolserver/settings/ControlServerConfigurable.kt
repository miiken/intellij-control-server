package io.miiken.intellijcontrolserver.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import io.miiken.intellijcontrolserver.ControlServerService
import io.miiken.intellijcontrolserver.config.ConfigLoader
import io.miiken.intellijcontrolserver.config.ServerConfig
import javax.swing.JComponent

class ControlServerConfigurable : Configurable {
    private var settingsComponent: ControlServerSettingsComponent? = null
    
    override fun getDisplayName(): String = "IntelliJ Control Server"
    
    override fun createComponent(): JComponent {
        settingsComponent = ControlServerSettingsComponent()
        return settingsComponent!!.getPanel()
    }
    
    override fun isModified(): Boolean {
        val component = settingsComponent ?: return false
        val currentConfig = ConfigLoader.load()
        
        return component.getPort() != currentConfig.port ||
                component.getHost() != currentConfig.host ||
                component.getAutoStart() != currentConfig.autoStart ||
                component.getLogLevel() != currentConfig.logLevel ||
                component.getEnableCors() != currentConfig.enableCors ||
                component.getEnableMcp() != currentConfig.enableMcp
    }
    
    override fun apply() {
        val component = settingsComponent ?: return
        
        val newConfig = ServerConfig(
            port = component.getPort(),
            host = component.getHost(),
            autoStart = component.getAutoStart(),
            logLevel = component.getLogLevel(),
            enableCors = component.getEnableCors(),
            enableMcp = component.getEnableMcp()
        )
        
        val errors = newConfig.validate()
        if (errors.isNotEmpty()) {
            Messages.showErrorDialog(
                "Invalid configuration:\n${errors.joinToString("\n")}",
                "Configuration Error"
            )
            return
        }
        
        ConfigLoader.save(newConfig)
        
        val service = ControlServerService.getInstance()
        if (service.isRunning()) {
            val restart = Messages.showYesNoDialog(
                "Control Server is currently running.\nRestart to apply changes?",
                "Restart Server?",
                Messages.getQuestionIcon()
            )
            
            if (restart == Messages.YES) {
                service.updateConfig(newConfig)
            }
        }
    }
    
    override fun reset() {
        val component = settingsComponent ?: return
        val config = ConfigLoader.load()
        
        component.setPort(config.port)
        component.setHost(config.host)
        component.setAutoStart(config.autoStart)
        component.setLogLevel(config.logLevel)
        component.setEnableCors(config.enableCors)
        component.setEnableMcp(config.enableMcp)
    }
    
    override fun disposeUIResources() {
        settingsComponent = null
    }
}

