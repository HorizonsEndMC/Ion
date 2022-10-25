package net.starlegacy.listener

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.Bukkit
import org.bukkit.event.Listener

abstract class SLEventListener : Listener {
	protected val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

	fun register() {
		Bukkit.getPluginManager().registerEvents(this, Ion)
		onRegister()
	}

	protected open fun onRegister() {}

	open fun supportsVanilla(): Boolean = false
}