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
                component.getEnableMcp() != currentConfig.enableMcp ||
                component.getRenameStringsInMethodBody() != currentConfig.renameStringsInMethodBody ||
                component.getRenameInAnnotations() != currentConfig.renameInAnnotations
    }
    
    override fun apply() {
        val component = settingsComponent ?: return
        
        val newConfig = ServerConfig(
            port = component.getPort(),
            host = component.getHost(),
            autoStart = component.getAutoStart(),
            logLevel = component.getLogLevel(),
            enableCors = component.getEnableCors(),
            enableMcp = component.getEnableMcp(),
            renameStringsInMethodBody = component.getRenameStringsInMethodBody(),
            renameInAnnotations = component.getRenameInAnnotations()
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
        } else if (newConfig.autoStart) {
            val start = Messages.showYesNoDialog(
                "Auto-start is enabled.\nStart the server now?",
                "Start Server?",
                Messages.getQuestionIcon()
            )
            
            if (start == Messages.YES) {
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
        component.setRenameStringsInMethodBody(config.renameStringsInMethodBody)
        component.setRenameInAnnotations(config.renameInAnnotations)
    }
    
    override fun disposeUIResources() {
        settingsComponent = null
    }
}

