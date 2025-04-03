package net.horizonsend.ion.server.features.world

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.manager.WorldMultiblockManager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.inputs.WorldIOManager
import net.horizonsend.ion.server.features.transport.manager.WorldTransportManager
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.features.world.configuration.DefaultWorldConfiguration
import net.horizonsend.ion.server.features.world.data.DataFixers
import net.horizonsend.ion.server.features.world.environment.Environment
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.mobs.CustomMobSpawner
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.DATA_VERSION
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FORBIDDEN_BLOCKS
import net.horizonsend.ion.server.miscellaneous.utils.mainThreadCheck
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
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
	val inputManager = WorldIOManager(this)
	val transportManager = WorldTransportManager(this).apply { load() }

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

		return chunks[key]
	}

	/**
	 * Gets the IonChunk at the specified coordinates if it is loaded
	 **/
	fun getChunkFromWorldcoordinates(x: Int, z: Int): IonChunk? {
		val key = Chunk.getChunkKey(x.shr(4), z.shr(4))

		return chunks[key]
	}

	/**
	 * Gets the IonChunk at the specified key if it is loaded
	 **/
	fun getChunk(key: Long): IonChunk? {
		return chunks[key]
	}

	fun isChunkLoaded(key: Long) = chunks.containsKey(key)
	fun isChunkLoaded(x: Int, z: Int) = chunks.containsKey(Chunk.getChunkKey(x, z))

	/**
	 * Adds the chunk
	 **/
	fun addChunk(chunk: IonChunk) {
		if (isChunkLoaded(chunk.locationKey)) {
			log.warn("Attempted to add a chunk that was already in the map!")
		}

		chunks[chunk.locationKey] = chunk
	}

	/**
	 * Removes the chunk
	 ***/
	fun removeChunk(chunk: Chunk): IonChunk? {
		val result = chunks.remove(chunk.chunkKey)

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
	var configuration: WorldSettings = loadConfiguration()

	fun reloadConfiguration() {
		configuration = loadConfiguration()
		enviornmentManager.reloadConfiguration()
	}

	/** Write the configuration to the disk */
	fun saveConfiguration() = Configuration.save(configuration, WORLD_CONFIGURATION_DIRECTORY, "${world.name}.json")

	/** Check if the world's configuration contains the flag */
	fun hasFlag(flag: WorldFlag): Boolean = configuration.flags.contains(flag)

	/** Get all environments applied to this world */
	val environments get() = configuration.environments

	val enviornmentManager = WorldEnvironmentManager(this)

	/** List of blocks that cannot be detected by starships */
	val detectionForbiddenBlocks = loadForbiddenBlocks()

	/** Contains custom mob spawning behavior */
	val customMonSpawner = CustomMobSpawner(this, configuration.customMobSpawns)

	/** Custom terrain generation handling, including space or nebulas */
	val terrainGenerator: IonWorldGenerator<*>? = configuration.terrainGenerationSettings?.buildGenerator(this)

	companion object : IonServerComponent() {
		private val WORLD_CONFIGURATION_DIRECTORY = ConfigurationFiles.configurationFolder.resolve("worlds").apply { mkdirs() }

		private val ionWorlds = mutableMapOf<World, IonWorld>()

		fun all() = ionWorlds.values

		operator fun get(world: World): IonWorld = ionWorlds[world] ?: throw IllegalStateException("Unregistered Ion World: $world!")

		fun register(world: World) = kotlin.runCatching {
			mainThreadCheck()

			if (ionWorlds.contains(world)) {
				throw IllegalStateException("Attempted to register server level which is already registered!")
			}

			val ionWorld = IonWorld(world)
			ionWorlds[world] = ionWorld

			DataFixers.handleWorldInit(ionWorld)
		}.onFailure {
			log.error("There was an error loading an Ion World [${world.key}]. The server will now shut down to prevent undefined behavior.")
			it.printStackTrace()
			Bukkit.shutdown()
		}

		fun unregisterAll() {
			mainThreadCheck()

			val iterator = ionWorlds.iterator()

			while (iterator.hasNext()) {
				val (_, ionWorld) = iterator.next()

				saveAll(ionWorld)
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

			saveAll(ionWorld)
			ionWorld.transportManager.unload()
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
		fun onMobSpawn(event: CreatureSpawnEvent) {
			event.location.world.ion.customMonSpawner.handleSpawnEvent(event)
		}

		@EventHandler
		fun onWorldSave(event: WorldSaveEvent) {
			saveAll(event.world.ion)
		}

		override fun onDisable() {
			for (world in ionWorlds.values) {
				saveAll(world)
			}
		}

		private fun saveAll(world: IonWorld) {
			world.transportManager.save()

			for ((_, chunk) in world.chunks) {
				chunk.save()
			}
		}

		/** Gets the world's Ion counterpart */
		val World.ion: IonWorld get() = get(this)
		fun World.hasFlag(flag: WorldFlag): Boolean = ion.hasFlag(flag)
//		fun World.environments(): Set<Environment> = ion.environments
	}

	private fun loadForbiddenBlocks(): LongOpenHashSet {
		val existing = world.persistentDataContainer.getOrDefault(FORBIDDEN_BLOCKS, LONG_ARRAY, longArrayOf())
		return LongOpenHashSet(existing)
	}

	fun saveForbiddenBlocks() {
		world.persistentDataContainer.set(FORBIDDEN_BLOCKS, LONG_ARRAY, detectionForbiddenBlocks.toLongArray())
	}

	fun loadConfiguration(): WorldSettings {
		return runCatching {
			Configuration.loadOrDefault(WORLD_CONFIGURATION_DIRECTORY, "${world.name}.json", DefaultWorldConfiguration[world.name])
		}.onFailure { exception ->
			IonServer.slF4JLogger.error("There was an error loading the world configuration for $this. To prevent undefiend behavior the server will now shut down.")
			exception.printStackTrace()
			Bukkit.shutdown()
		}.getOrThrow()
	}

	fun getAllChunks() = chunks.values

	override fun toString(): String {
		return "IonWorld[${world.key}]"
	}
}
