package net.starlegacy.listener

import net.starlegacy.PLUGIN
import org.bukkit.Bukkit
import org.bukkit.event.Listener

abstract class SLEventListener : Listener {
    protected val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    protected val plugin get() = PLUGIN

    fun register() {
        Bukkit.getPluginManager().registerEvents(this, PLUGIN)
        onRegister()
    }

    protected open fun onRegister() {}

    open fun supportsVanilla(): Boolean = false
}
