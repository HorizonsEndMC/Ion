package net.horizonsend.ion.server.features.starship

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.misc.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipFactory
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.subsystem.LandingGearSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq
import java.io.File
import java.util.UUID

object DeactivatedPlayerStarships : IonServerComponent() {
	private val DEACTIVATED_SHIP_WORLD_CACHES: MutableMap<World, DeactivatedShipWorldCache> = Object2ObjectOpenHashMap()

	private fun getCache(world: World) = requireNotNull(DEACTIVATED_SHIP_WORLD_CACHES[world])

	operator fun get(world: World, x: Int, y: Int, z: Int): StarshipData? {
		synchronized(lock) {
			return getCache(world)[x, y, z]
		}
	}

	fun getInChunk(chunk: Chunk): List<StarshipData> {
		synchronized(lock) {
			return getCache(chunk.world).getInChunk(chunk)
		}
	}

	fun getContaining(world: World, x: Int, y: Int, z: Int): StarshipData? {
		synchronized(lock) {
			return getCache(world).getContaining(x, y, z)
		}
	}

	fun getLockedContaining(world: World, x: Int, y: Int, z: Int): StarshipData? {
		synchronized(lock) {
			return getCache(world).getLockedContaining(x, y, z)
		}
	}

	fun createAsync(
		world: World,
		x: Int,
		y: Int,
		z: Int,
		playerId: UUID,
		name: String? = null,
		callback: (PlayerStarshipData) -> Unit
	) = Tasks.async {
		synchronized(lock) {
			require(getCache(world)[x, y, z] == null)
			val captain = playerId.slPlayerId
			val type = StarshipType.SHUTTLE
			val id = objId<PlayerStarshipData>()
			val blockKey = blockKey(x, y, z)
			val worldName = world.name
			val autoLock = Bukkit.getPlayer(playerId)?.hasProtection() == true

			val data = PlayerStarshipData(
				_id = id,
				captain = captain,
				starshipType = type.name,
				serverName = IonServer.configuration.serverName,
				levelName = worldName,
				blockKey = blockKey,
				name = name,
				isLockEnabled = autoLock
			)
			PlayerStarshipData.add(data)
			getCache(world).add(data)

			Tasks.sync { callback(data) }
		}
	}

	/** This method creates mostly meaningless data for AI ships **/
	fun createAsync(
		world: World,
		x: Int,
		y: Int,
		z: Int,
		name: String? = null,
		callback: (StarshipData) -> Unit
	) = createAsync(world, x, y, z, UUID.randomUUID(), name, callback)

	fun getSavedState(data: StarshipData): StarshipState? {
		return getCache(data.bukkitWorld()).savedStateCache[data].orElse(null)
	}

	fun removeState(data: StarshipData) {
		synchronized(lock) {
			getCache(data.bukkitWorld()).removeState(data)
		}
	}

	fun updateState(data: StarshipData, state: StarshipState) {
		synchronized(lock) {
			getCache(data.bukkitWorld()).updateState(data, state)
		}
	}

	fun updateType(data: StarshipData, newType: StarshipType) {
		data.starshipType = newType.name

		Tasks.async {
			data.companion().setType(data._id, newType.name)
		}

		// remove the current state in case the new type no longer matches the ship's state
		removeState(data)
	}

	fun updateName(data: StarshipData, newName: String?) {
		data.name = newName

		Tasks.async {
			data.companion().setName(data._id, newName)
		}
	}

	fun updateLockEnabled(data: StarshipData, newValue: Boolean) {
		data.isLockEnabled = newValue

		Tasks.async {
			data.companion().setLockEnabled(data._id, newValue)
		}
	}

	fun addPilot(data: PlayerStarshipData, pilotID: SLPlayerId) {
		data.pilots += pilotID
		Tasks.async {
			PlayerStarshipData.updateById(data._id, addToSet(PlayerStarshipData::pilots, pilotID))
		}
	}

	override fun onEnable() {
		for (world in IonServer.server.worlds) {
			load(world)
		}

		listen<WorldLoadEvent> { event ->
			load(event.world)
		}

		listen<WorldUnloadEvent> { event ->
			DEACTIVATED_SHIP_WORLD_CACHES.remove(event.world)
		}
	}

	private fun load(world: World) {
		val cache = DeactivatedShipWorldCache(world)
		// retrieve all starship data from the database and add it to the cache
		PlayerStarshipData.find(PlayerStarshipData::levelName eq world.name).forEach { cache.add(it) }
		DEACTIVATED_SHIP_WORLD_CACHES[world] = cache
	}

	fun getSaveFile(world: World, data: StarshipData): File {
		return File(getCache(world).dataFolder, "${data._id}.dat")
	}

	private val lock = Any()

	fun activateAsync(
		feedbackDestination: Audience,
		data: StarshipData,
		state: StarshipState,
		carriedShips: List<StarshipData>,
		callback: (ActiveControlledStarship) -> Unit = {}
	): Unit = Tasks.async {
		synchronized(lock) {
			require(!carriedShips.contains(data)) { "Carried ships can't contain the ship itself!" }

			val world: World = data.bukkitWorld()
			val cache: DeactivatedShipWorldCache = getCache(world)

			if (cache[data.blockKey] != data) {
				return@async // probably already piloted bc they spam clicked
			}

			data.companion().remove(data._id)

			cache.remove(data)

			val carriedShipMap = captureCarriedShips(carriedShips, cache)

			Tasks.sync {
				val starship = ActiveStarshipFactory.createPlayerStarship(
					feedbackDestination,
					data,
					state.blockMap.keys,
					carriedShipMap
				) ?: return@sync

				ActiveStarships.add(starship)
				callback.invoke(starship)
			}
		}
	}

	private fun captureCarriedShips(carriedShips: List<StarshipData>, cache: DeactivatedShipWorldCache): MutableMap<StarshipData, LongOpenHashSet> {
		val carriedShipMap = mutableMapOf<StarshipData, LongOpenHashSet>()

		for (carried: StarshipData in carriedShips) {
			carried.companion().remove(carried._id)

			cache.remove(carried)

			val state: StarshipState? = getSavedState(carried)

			val blocks = if (state == null) LongOpenHashSet(0) else LongOpenHashSet(state.blockMap.keys)

			carriedShipMap[carried] = blocks
		}

		return carriedShipMap
	}

	fun deactivateAsync(starship: ActiveControlledStarship, callback: () -> Unit = {}) {
		Tasks.checkMainThread()

		if (PilotedStarships.isPiloted(starship)) {
			PilotedStarships.unpilot(starship)
		}

		Tasks.async {
			deactivateNow(starship)
			Tasks.sync(callback)
		}
	}

	fun deactivateNow(starship: ActiveControlledStarship) {
		if (PilotedStarships.isPiloted(starship)) {
			Tasks.getSyncBlocking {
				PilotedStarships.unpilot(starship)
			}
		}

		val world: World = starship.world

		val carriedShipStateMap = Object2ObjectOpenHashMap<StarshipData, StarshipState>()

		val state: StarshipState = Tasks.getSyncBlocking {
			// this needs to be removed sync!
			ActiveStarships.remove(starship)

			val landingGear = starship.subsystems.filterIsInstance<LandingGearSubsystem>()
			for (landingGearSubsystem in landingGear) {
				landingGearSubsystem.setExtended(true)
			}

			for ((ship: StarshipData, blocks: Set<Long>) in starship.carriedShips) {
				if (!blocks.isEmpty()) {
					carriedShipStateMap[ship] = StarshipState.createFromBlocks(world, blocks)
				}
			}

			return@getSyncBlocking StarshipState.createFromActiveShip(starship)
		}

		saveDeactivatedData(world, starship, state, carriedShipStateMap)
	}

	private fun saveDeactivatedData(
		world: World,
		starship: ActiveControlledStarship,
		state: StarshipState,
		carriedShipStateMap: Object2ObjectOpenHashMap<StarshipData, StarshipState>
	) {
		synchronized(lock) {
			val cache: DeactivatedShipWorldCache = getCache(world)

			val data = starship.data
			data.lastUsed = System.currentTimeMillis()

			// this prevents it from being added to the chunk->saved ship cache in worldCache.add
			data.containedChunks = null
			// add to the deactivated ship world cache
			cache.add(data)

			// this sets the contained chunks to those of the provided state, and saved the state to disk
			cache.updateState(data, state)
			data.companion().add(data)

			for (carriedData: StarshipData in starship.carriedShips.keys) {
				carriedData.containedChunks = null
				cache.add(carriedData)
				carriedShipStateMap[carriedData]?.let { carriedDataState: StarshipState ->
					cache.updateState(carriedData, carriedDataState)
				}

				carriedData.companion().add(carriedData)
			}
		}
	}

	fun destroyAsync(data: StarshipData, callback: () -> Unit = {}): Unit = Tasks.async {
		synchronized(lock) {
			destroy(data)

			Tasks.sync(callback)
		}
	}

	fun destroyManyAsync(datas: List<StarshipData>, callback: () -> Unit = {}): Unit = Tasks.async {
		synchronized(lock) {
			for (data in datas) {
				destroy(data)
			}

			Tasks.sync(callback)
		}
	}

	private fun destroy(data: StarshipData) {
		require(ActiveStarships[data._id] == null) { "Can't delete an active starship, but tried deleting ${data._id}" }

		val world: World = data.bukkitWorld()
		val cache: DeactivatedShipWorldCache = getCache(world)
		cache.remove(data)
		getSaveFile(world, data).delete()

		data.companion().remove(data._id)
	}
}
