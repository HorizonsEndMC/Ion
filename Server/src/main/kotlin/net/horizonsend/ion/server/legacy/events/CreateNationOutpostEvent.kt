package net.horizonsend.ion.server.legacy.events

import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.Nation
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CreateNationOutpostEvent(val player: Player, val nationID: Oid<Nation>) : Event(true) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}