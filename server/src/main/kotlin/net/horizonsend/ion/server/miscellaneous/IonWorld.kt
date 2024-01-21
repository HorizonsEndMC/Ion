package net.horizonsend.ion.server.miscellaneous

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.mainThreadCheck
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.event.world.WorldUnloadEvent
import kotlin.DeprecationLevel.ERROR

class IonWorld private constructor(
	val world: World,
	val starships: MutableList<ActiveStarship> = mutableListOf()
) {
	val configuration: WorldSettings by lazy { Configuration.load(IonServer.configurationFolder, "${world.name}.json") }

	fun saveConfiguration() = Configuration.save(configuration, IonServer.configurationFolder, "${world.name}.json")

	fun hasFlag(flag: WorldFlag): Boolean = configuration.flags.contains(flag)

	//TODO
	// - Terrain Generator
	// - Environment Provider
	// - IonChunks
	//   - Wires
	//   - Explosion Reversal
	// - Area Shields
	// -
	// -

	companion object : SLEventListener() {
		private val ionWorlds = mutableMapOf<World, IonWorld>()

		operator fun get(world: World): IonWorld = ionWorlds[world] ?: throw IllegalStateException("Unregistered Ion World: $world!")

		fun World.ion(): IonWorld = get(this)

		fun register(world: World) {
			mainThreadCheck()

			if (ionWorlds.contains(world)) {
				throw IllegalStateException("Attempted to register server level which is already registered!")
			}

			ionWorlds[world] = IonWorld(world)
			AreaShields.loadData(world)
		}

		fun unregisterAll() {
			mainThreadCheck()

			ionWorlds.clear()
		}

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		fun onWorldInitEvent(event: WorldInitEvent) {
			mainThreadCheck()

			register(event.world)
		}

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		fun onWorldUnloadEvent(event: WorldUnloadEvent) {
			mainThreadCheck()

			ionWorlds.remove(event.world)
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

		@EventHandler
		fun onWorldSave(event: WorldSaveEvent) {
//			TODO
		}
	}

	@Serializable
	data class WorldSettings(
		val flags: MutableSet<WorldFlag> = mutableSetOf()
	)

	enum class WorldFlag {
		SPACE_ENVIRONMENT,
		ALLOW_SPACE_STATIONS,
		ALLOW_AI_SPAWNS,
	}
}
