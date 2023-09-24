package net.horizonsend.ion.server.miscellaneous

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.mainThreadCheck
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.server.level.ServerLevel
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldUnloadEvent
import kotlin.DeprecationLevel.ERROR

class IonWorld private constructor(
	val serverLevel: ServerLevel,

	val starships: MutableList<ActiveStarship> = mutableListOf()
) {
	companion object : SLEventListener() {
		private val ionWorlds = mutableMapOf<ServerLevel, IonWorld>()

		operator fun get(serverLevel: ServerLevel): IonWorld = ionWorlds[serverLevel]!!

		fun register(serverLevel: ServerLevel) {
			mainThreadCheck()

			if (ionWorlds.contains(serverLevel)) {
				throw IllegalStateException("Attempted to register server level which is already registered!")
			}

			ionWorlds[serverLevel] = IonWorld(serverLevel)
			AreaShields.loadData()
		}

		fun unregisterAll() {
			mainThreadCheck()

			ionWorlds.clear()
		}

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		fun onWorldInitEvent(event: WorldInitEvent) {
			mainThreadCheck()

			register(event.world.minecraft)
		}

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		fun onWorldUnloadEvent(event: WorldUnloadEvent) {
			mainThreadCheck()

			ionWorlds.remove(event.world.minecraft)
		}

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		fun onServerTickStartEvent(@Suppress("UNUSED_PARAMETER") event: ServerTickStartEvent) {
			mainThreadCheck()

			for (ionWorld in ionWorlds.values)
			for (starship in ionWorld.starships) {
				val result = runCatching(starship::tick).exceptionOrNull() ?: continue
				log.warn("Exception while ticking starship!", result)
			}
		}
	}
}
