package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/** Called prior to
 * @see StarshipExplodeEvent
 * Contains information about the ship prior to sinking
 **/
class StarshipSunkEvent(val starship: Starship, val previousController: Controller) : Event() {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
