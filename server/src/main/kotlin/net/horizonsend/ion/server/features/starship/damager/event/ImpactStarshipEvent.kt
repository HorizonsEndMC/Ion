package net.horizonsend.ion.server.features.starship.damager.event

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ImpactStarshipEvent(
	val damager: Damager,
	val starship: ActiveStarship
) : Event(), Cancellable {
	private var isCancelled = false

	override fun isCancelled(): Boolean = isCancelled

	override fun getHandlers(): HandlerList = handlerList

	override fun setCancelled(cancel: Boolean) {
		isCancelled = cancel
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
