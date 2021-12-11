package net.starlegacy.feature.starship

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.starlegacy.SLComponent
import net.starlegacy.database.objId
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarshipFactory
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.Tasks
import net.starlegacy.util.blockKey
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.io.File
import java.util.UUID

object DeactivatedPlayerStarships : SLComponent() {
    private val DEACTIVATED_SHIP_WORLD_CACHES: MutableMap<World, DeactivatedShipWorldCache> = Object2ObjectOpenHashMap()

    private fun getCache(world: World) = requireNotNull(DEACTIVATED_SHIP_WORLD_CACHES[world])

    operator fun get(world: World, x: Int, y: Int, z: Int): PlayerStarshipData? {
        synchronized(lock) {
            return getCache(world)[x, y, z]
        }
    }

    fun getInChunk(chunk: Chunk): List<PlayerStarshipData> {
        synchronized(lock) {
            return getCache(chunk.world).getInChunk(chunk)
        }
    }

    fun getLockedContaining(world: World, x: Int, y: Int, z: Int): PlayerStarshipData? {
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
        callback: (PlayerStarshipData) -> Unit
    ) = Tasks.async {
        synchronized(lock) {
            require(getCache(world)[x, y, z] == null)
            val captain = playerId.slPlayerId
            val type = StarshipType.SHUTTLE
            val id = objId<PlayerStarshipData>()
            val blockKey = blockKey(x, y, z)
            val worldName = world.name
            val data = PlayerStarshipData(id, captain, type, worldName, blockKey)
            PlayerStarshipData.add(data)
            getCache(world).add(data)

            Tasks.sync { callback(data) }
        }
    }

    fun getSavedState(data: PlayerStarshipData): PlayerStarshipState? {
        return getCache(data.bukkitWorld()).savedStateCache[data].orElse(null)
    }

    fun removeState(data: PlayerStarshipData) {
        synchronized(lock) {
            getCache(data.bukkitWorld()).removeState(data)
        }
    }

    fun updateState(data: PlayerStarshipData, state: PlayerStarshipState) {
        synchronized(lock) {
            getCache(data.bukkitWorld()).updateState(data, state)
        }
    }

    fun updateType(data: PlayerStarshipData, newType: StarshipType) {
        data.type = newType

        Tasks.async {
            PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::type, newType))
        }

        // remove the current state in case the new type no longer matches the ship's state
        removeState(data)
    }

    fun updateLockEnabled(data: PlayerStarshipData, newValue: Boolean) {
        data.isLockEnabled = newValue

        Tasks.async {
            PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::isLockEnabled, newValue))
        }
    }

    fun addPilot(data: PlayerStarshipData, pilotID: SLPlayerId) {
        data.pilots += pilotID
        Tasks.async {
            PlayerStarshipData.updateById(data._id, addToSet(PlayerStarshipData::pilots, pilotID))
        }
    }

    override fun onEnable() {
        for (world in plugin.server.worlds) {
            load(world)
        }

        subscribe<WorldLoadEvent> { event ->
            load(event.world)
        }

        subscribe<WorldUnloadEvent> { event ->
            DEACTIVATED_SHIP_WORLD_CACHES.remove(event.world)
        }
    }

    private fun load(world: World) {
        val cache = DeactivatedShipWorldCache(world)
        // retrieve all starship data from the database and add it to the cache
        PlayerStarshipData.find(PlayerStarshipData::world eq world.name).forEach { cache.add(it) }
        DEACTIVATED_SHIP_WORLD_CACHES[world] = cache
    }

    fun getSaveFile(world: World, data: PlayerStarshipData): File {
        return File(getCache(world).dataFolder, "${data._id}.dat")
    }

    private val lock = Any()

    fun activateAsync(
        data: PlayerStarshipData, state: PlayerStarshipState, carriedShips: List<PlayerStarshipData>,
        callback: (ActivePlayerStarship) -> Unit = {}
    ): Unit = Tasks.async {
        synchronized(lock) {
            require(!carriedShips.contains(data)) { "Carried ships can't contain the ship itself!" }

            val world: World = data.bukkitWorld()
            val cache: DeactivatedShipWorldCache = getCache(world)

            if (cache[data.blockKey] != data) {
                return@async // probably already piloted bc they spam clicked
            }

            PlayerStarshipData.remove(data._id)

            cache.remove(data)

            val carriedShipMap = captureCarriedShips(carriedShips, cache)

            Tasks.sync {
                val starship = ActiveStarshipFactory.createPlayerStarship(data, state.blockMap.keys, carriedShipMap)
                ActiveStarships.add(starship)
                callback.invoke(starship)
            }
        }
    }

    private fun captureCarriedShips(carriedShips: List<PlayerStarshipData>, cache: DeactivatedShipWorldCache)
            : MutableMap<PlayerStarshipData, LongOpenHashSet> {
        val carriedShipMap = mutableMapOf<PlayerStarshipData, LongOpenHashSet>()

        for (carried: PlayerStarshipData in carriedShips) {
            PlayerStarshipData.remove(carried._id)
            cache.remove(carried)
            val state: PlayerStarshipState? = getSavedState(carried)
            val blocks = if (state == null) LongOpenHashSet(0) else LongOpenHashSet(state.blockMap.keys)
            carriedShipMap[carried] = blocks
        }

        return carriedShipMap
    }

    fun deactivateAsync(starship: ActivePlayerStarship, callback: () -> Unit = {}) {
        Tasks.checkMainThread()

        if (PilotedStarships.isPiloted(starship)) {
            PilotedStarships.unpilot(starship)
        }

        Tasks.async {
            deactivateNow(starship)
            Tasks.sync(callback)
        }
    }

    fun deactivateNow(starship: ActivePlayerStarship) {
        if (PilotedStarships.isPiloted(starship)) {
            Tasks.getSyncBlocking {
                PilotedStarships.unpilot(starship)
            }
        }

        val world: World = starship.world

        val carriedShipStateMap = Object2ObjectOpenHashMap<PlayerStarshipData, PlayerStarshipState>()

        val state: PlayerStarshipState = Tasks.getSyncBlocking {
            // this needs to be removed sync!
            ActiveStarships.remove(starship)

            for ((ship: PlayerStarshipData, blocks: Set<Long>) in starship.carriedShips) {
                if (!blocks.isEmpty()) {
                    carriedShipStateMap[ship] = PlayerStarshipState.createFromBlocks(world, blocks)
                }
            }

            return@getSyncBlocking PlayerStarshipState.createFromActiveShip(starship)
        }

        saveDeactivatedData(world, starship, state, carriedShipStateMap)
    }

    private fun saveDeactivatedData(
        world: World,
        starship: ActivePlayerStarship,
        state: PlayerStarshipState,
        carriedShipStateMap: Object2ObjectOpenHashMap<PlayerStarshipData, PlayerStarshipState>
    ) {
        synchronized(lock) {
            val cache: DeactivatedShipWorldCache = getCache(world)

            val data: PlayerStarshipData = starship.data
            data.lastUsed = System.currentTimeMillis()

            // this prevents it from being added to the chunk->saved ship cache in worldCache.add
            data.containedChunks = null
            // add to the deactivated ship world cache
            cache.add(data)
            // this sets the contained chunks to those of the provided state, and saved the state to disk
            cache.updateState(data, state)

            PlayerStarshipData.add(data)

            for (carriedData: PlayerStarshipData in starship.carriedShips.keys) {
                carriedData.containedChunks = null
                cache.add(carriedData)
                carriedShipStateMap[carriedData]?.let { carriedDataState: PlayerStarshipState ->
                    cache.updateState(carriedData, carriedDataState)
                }
                PlayerStarshipData.add(carriedData)
            }
        }
    }

    fun destroyAsync(data: PlayerStarshipData, callback: () -> Unit = {}): Unit = Tasks.async {
        synchronized(lock) {
            destroy(data)

            Tasks.sync(callback)
        }
    }

    fun destroyManyAsync(datas: List<PlayerStarshipData>, callback: () -> Unit = {}): Unit = Tasks.async {
        synchronized(lock) {
            for (data in datas) {
                destroy(data)
            }

            Tasks.sync(callback)
        }
    }

    private fun destroy(data: PlayerStarshipData) {
        require(ActiveStarships[data._id] == null) { "Can't delete an active starship, but tried deleting ${data._id}" }

        val world: World = data.bukkitWorld()
        val cache: DeactivatedShipWorldCache = getCache(world)
        cache.remove(data)
        getSaveFile(world, data).delete()
        PlayerStarshipData.remove(data._id)
    }
}
