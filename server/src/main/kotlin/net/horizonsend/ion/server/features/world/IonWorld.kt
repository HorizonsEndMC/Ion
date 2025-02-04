package net.horizonsend.ion.server.features.world

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.multiblock.manager.WorldMultiblockManager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.nodes.inputs.WorldInputManager
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.features.world.configuration.DefaultWorldConfiguration
import net.horizonsend.ion.server.features.world.data.DataFixers
import net.horizonsend.ion.server.features.world.environment.Environment
import net.horizonsend.ion.server.features.world.environment.mobs.CustomMobSpawner
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.DATA_VERSION
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FORBIDDEN_BLOCKS
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.mainThreadCheck
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.LONG_ARRAY
import kotlin.DeprecationLevel.ERROR

class IonWorld private constructor(
	val world: World,
	val starships: MutableList<ActiveStarship> = mutableListOf()
) {
	var dataVersion = world.persistentDataContainer.getOrDefault(DATA_VERSION, INTEGER, 0)
		set	(value) {
			world.persistentDataContainer.set(DATA_VERSION, INTEGER, value)
			field = value
		}

	val multiblockManager = WorldMultiblockManager(this)
	val inputManager = WorldInputManager(this)

	private val chunkLock = Any()

	/**
	 * Key: The location of the chunk packed into a long
	 *
	 * Value: The IonChunk at that location
	 **/
	private val chunks: Long2ObjectOpenHashMap<IonChunk> = Long2ObjectOpenHashMap()

	/**
	 * Gets the IonChunk at the specified coordinates if it is loaded
	 **/
	fun getChunk(x: Int, z: Int): IonChunk? {
		val key = Chunk.getChunkKey(x, z)

		return synchronized(chunkLock) { chunks[key] }
	}

	/**
	 * Gets the IonChunk at the specified key if it is loaded
	 **/
	fun getChunk(key: Long): IonChunk? {
		return synchronized(chunkLock) { chunks[key] }
	}

	fun isChunkLoaded(key: Long) = synchronized(chunkLock) { chunks.keys.contains(key) }

	/**
	 * Adds the chunk
	 **/
	fun addChunk(chunk: IonChunk) {
		if (isChunkLoaded(chunk.locationKey)) {
			log.warn("Attempted to add a chunk that was already in the map!")
		}

		synchronized(chunkLock) { chunks[chunk.locationKey] = chunk }
	}

	/**
	 * Removes the chunk
	 ***/
	fun removeChunk(chunk: Chunk): IonChunk? {
		val result = synchronized(chunkLock) { chunks.remove(chunk.chunkKey) }

		if (result == null) {
			log.warn("Chunk removed that was not in the map!")
		}

		return result
	}

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
	val configuration: WorldSettings by lazy {
		Configuration.loadOrDefault(WORLD_CONFIGURATION_DIRECTORY, "${world.name}.json", DefaultWorldConfiguration[world.name])
	}

	/** Write the configuration to the disk */
	fun saveConfiguration() = Configuration.save(configuration, WORLD_CONFIGURATION_DIRECTORY, "${world.name}.json")

	/** Check if the world's configuration contains the flag */
	fun hasFlag(flag: WorldFlag): Boolean = configuration.flags.contains(flag)

	/** Get all environments applied to this world */
	val environments get() = configuration.environments

	/** Get all players on the inner world */
	val players: List<Player> get() = world.players

	val detectionForbiddenBlocks = loadForbiddenBlocks()

	val customMonSpawner = CustomMobSpawner(this, configuration.customMobSpawns)

	//TODO
	// - Terrain Generator
	// - Worldborder injection

	companion object : IonServerComponent() {
		private val WORLD_CONFIGURATION_DIRECTORY = ConfigurationFiles.configurationFolder.resolve("worlds").apply { mkdirs() }

		private val ionWorlds = mutableMapOf<World, IonWorld>()

		operator fun get(world: World): IonWorld = ionWorlds[world] ?: throw IllegalStateException("Unregistered Ion World: $world!")

		fun register(world: World) {
			mainThreadCheck()

			if (ionWorlds.contains(world)) {
				throw IllegalStateException("Attempted to register server level which is already registered!")
			}

			val ionWorld = IonWorld(world)
			ionWorlds[world] = ionWorld

			DataFixers.handleWorldInit(ionWorld)

			ionWorld.configuration.environments.forEach { it.setup() }
			Tasks.syncRepeat(10, 10, ionWorld::tickEnvironments)
		}

		fun unregisterAll() {
			mainThreadCheck()

			val iterator = ionWorlds.iterator()

			while (iterator.hasNext()) {
				val (_, ionWorld) = iterator.next()

				saveAllChunks(ionWorld)
				iterator.remove()
			}
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

			val bukkitWorld = event.world
			val ionWorld = ionWorlds[bukkitWorld]!!

			saveAllChunks(ionWorld)
			ionWorlds.remove(bukkitWorld)
		}

		@Deprecated("Event Listener", level = ERROR)
		@EventHandler
		fun onServerTickStartEvent(@Suppress("UNUSED_PARAMETER") event: ServerTickStartEvent) {
			mainThreadCheck()

			for (ionWorld in ionWorlds.values) {
				for (starship in ionWorld.starships) {
					val result = runCatching(starship::tick).exceptionOrNull() ?: continue
					log.warn("Exception while ticking starship!", result)
				}
			}
		}

		@EventHandler
		fun onWorldSave(event: WorldSaveEvent) {
			saveAllChunks(event.world.ion)
		}

		override fun onDisable() {
			for (world in ionWorlds.values) {
				saveAllChunks(world)
			}
		}

		private fun saveAllChunks(world: IonWorld) {
			for ((_, chunk) in world.chunks) {
				chunk.save()
			}
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

	private fun loadForbiddenBlocks(): LongOpenHashSet {
		val existing = world.persistentDataContainer.getOrDefault(FORBIDDEN_BLOCKS, LONG_ARRAY, longArrayOf())
		return LongOpenHashSet(existing)
	}

	fun saveForbiddenBlocks() {
		world.persistentDataContainer.set(FORBIDDEN_BLOCKS, LONG_ARRAY, detectionForbiddenBlocks.toLongArray())
	}

	fun getAllChunks() = chunks
}
