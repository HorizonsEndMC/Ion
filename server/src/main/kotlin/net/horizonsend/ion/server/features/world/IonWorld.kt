package net.horizonsend.ion.server.features.world

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.world.environment.Environment
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.mainThreadCheck
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.event.world.WorldUnloadEvent
import kotlin.DeprecationLevel.ERROR

class IonWorld private constructor(
	val world: World,
	val starships: MutableList<ActiveStarship> = mutableListOf()
) {
	/**
	 * The world configuration
	 *
	 * Contains:
	 *  - Flags
	 *  - Environments
	 *  - Generation Settings
	 *
	 * @see
	 * @see Environment
	 * @see WorldSettings
	 **/
	val configuration: WorldSettings by lazy { Configuration.load(WORLD_CONFIGURATION_DIRECTORY, "${world.name}.json") }

	/** Write the configuration to the disk */
	fun saveConfiguration() = Configuration.save(configuration, WORLD_CONFIGURATION_DIRECTORY, "${world.name}.json")

	/** Check if the world's configuration contains the flag */
	fun hasFlag(flag: WorldFlag): Boolean = configuration.flags.contains(flag)

	/** Get all environments applied to this world */
	val environments get() = configuration.environments

	/** Get all players on the inner world */
	val players: List<Player> get() = world.players

	//TODO
	// - Terrain Generator
	// - IonChunks
	//   - Wires
	//   - Explosion Reversal
	// - Area Shields
	// -
	// -

	companion object : SLEventListener() {
		private val WORLD_CONFIGURATION_DIRECTORY = IonServer.configurationFolder.resolve("worlds").apply { mkdirs() }

		private val ionWorlds = mutableMapOf<World, IonWorld>()

		operator fun get(world: World): IonWorld = ionWorlds[world] ?: throw IllegalStateException("Unregistered Ion World: $world!")

		fun register(world: World) {
			mainThreadCheck()

			if (ionWorlds.contains(world)) {
				throw IllegalStateException("Attempted to register server level which is already registered!")
			}

			val ionWorld = IonWorld(world)
			ionWorlds[world] = ionWorld

			AreaShields.loadData(world)
			Tasks.syncRepeat(10, 10, ionWorld::tickEnvironments)
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

		/** Gets the world's Ion counterpart */
		val World.ion: IonWorld get() = get(this)
		fun World.hasFlag(flag: WorldFlag): Boolean = ion.hasFlag(flag)
		fun World.environments(): Set<Environment> = ion.environments
	}

	private fun tickEnvironments() {
		for (environment in environments) {
			players.forEach(environment::tickPlayer)
		}
	}
}
