package net.horizonsend.ion.server.listener

import net.horizonsend.ion.server.IonServer
import org.bukkit.Bukkit
import org.bukkit.event.Listener

/** Unbound listeners
 * Be sure to register them in [net.horizonsend.ion.server.miscellaneous.registrations.Listeners](server/src/main/kotlin/net/horizonsend/ion/server/miscellaneous/registrations/Listeners.kt)*/
abstract class SLEventListener : Listener {
	protected val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

	fun register() {
		Bukkit.getPluginManager().registerEvents(this, IonServer)
		onRegister()
	}

	protected open fun onRegister() {}

	open fun supportsVanilla(): Boolean = false
}
