package net.horizonsend.ion.server

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.server.level.ServerLevel
import net.starlegacy.feature.machine.AreaShields
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldUnloadEvent
import kotlin.DeprecationLevel.ERROR

class IonWorld private constructor(
	val serverLevel: ServerLevel,

	val starships: MutableList<Starship> = mutableListOf()
) {
	companion object : Listener {
		private val ionWorlds = mutableMapOf<ServerLevel, IonWorld>()

		operator fun get(serverLevel: ServerLevel): IonWorld = ionWorlds[serverLevel]!!

		fun register(serverLevel: ServerLevel) {
			if (ionWorlds.contains(serverLevel)) {
				throw IllegalStateException("Attempted to register server level which is already registered!")
			}

			ionWorlds[serverLevel] = IonWorld(serverLevel)
			AreaShields.loadData()
		}

		fun unregister(serverLevel: ServerLevel) {
			ionWorlds.remove(serverLevel)
		}

		fun unregisterAll() {
			ionWorlds.clear()
		}

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		@Suppress("DeprecatedCallableAddReplaceWith")
		fun onWorldInitEvent(event: WorldInitEvent) = register(event.world.minecraft)

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		@Suppress("DeprecatedCallableAddReplaceWith")
		fun onWorldUnloadEvent(event: WorldUnloadEvent) = unregister(event.world.minecraft)

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		fun onServerTickStartEvent(@Suppress("UNUSED_PARAMETER") event: ServerTickStartEvent) {
			for (ionWorld in ionWorlds.values) for (starship in ionWorld.starships) {
				try {
					starship.tick()
				} catch (exception: Exception) {
					IonServer.slF4JLogger.warn("Exception while ticking starship!", exception)
				}
			}
		}
	}
}
