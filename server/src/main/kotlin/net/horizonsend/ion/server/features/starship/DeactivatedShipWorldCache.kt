package net.horizonsend.ion.server.features.starship

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.chunkKey
import net.horizonsend.ion.server.miscellaneous.utils.orNull
import org.bukkit.Chunk
import org.bukkit.World
import org.litote.kmongo.json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Optional
import java.util.concurrent.TimeUnit

/**
 * For keeping track of all deactivated ships
 */
class DeactivatedShipWorldCache(world: World) {
	private val worldName = world.name
	val dataFolder = File(world.worldFolder, "data/starlegacy/starship_saves").also { it.mkdirs() }

	private val blockKeyMap: MutableMap<Long, StarshipData> = Long2ObjectOpenHashMap()
	private val chunkKeyMap: Multimap<Long, StarshipData> = HashMultimap.create()

	private val mutex = Any()

	fun add(data: StarshipData): Unit = synchronized(mutex) {
		val blockKey: Long = data.blockKey

		check(!blockKeyMap.containsKey(blockKey)) {
			"$worldName already has starship data at ${Vec3i(blockKey)} (existing: ${blockKeyMap[blockKey]}, tried adding: $data)"
		}

		blockKeyMap[blockKey] = data

		data.containedChunks?.forEach { chunkKey ->
			chunkKeyMap[chunkKey].add(data)
		}
	}

	fun remove(data: StarshipData): Unit = synchronized(mutex) {
		val blockKey: Long = data.blockKey
		val existing: StarshipData? = blockKeyMap[blockKey]

		requireNotNull(existing) {
			"$worldName does not have starship data at ${Vec3i(blockKey)}, " +
				"but ${data._id} was attempted to be removed. " +
				"Full json: ${data.json}"
		}

		require(existing._id == data._id) {
			"$worldName does have starship data at ${Vec3i(blockKey)}, " +
				"but it's a different ID! " +
				"Tried removing ${data._id} but found ${existing._id}"
		}

		blockKeyMap.remove(data.blockKey)

		data.containedChunks?.forEach { chunkKey ->
			chunkKeyMap[chunkKey].remove(data)
		}

		savedStateCache.invalidate(data)
	}

	val savedStateCache: LoadingCache<StarshipData, Optional<StarshipState>> = CacheBuilder.newBuilder()
		.weakKeys()
		.expireAfterAccess(1, TimeUnit.HOURS)
		.build(
			CacheLoader.from { data: StarshipData? ->
				if (data != null) {
					val saveFile = DeactivatedPlayerStarships.getSaveFile(world, data)
					if (saveFile.exists()) {
						val result = FileInputStream(saveFile).use {
							StarshipState.readFromStream(it)
						}
						Optional.of(result)
					} else {
						Optional.empty()
					}
				} else {
					Optional.empty()
				}
			}
		)

	fun removeState(data: StarshipData): Unit = synchronized(mutex) {
		data.containedChunks?.forEach { chunkKeyMap[it].remove(data) }

		data.companion().updateChunks(data._id, null)

		DeactivatedPlayerStarships.getSaveFile(data.bukkitWorld(), data).delete()
		savedStateCache.put(data, Optional.empty())
	}

	fun updateState(data: StarshipData, state: StarshipState): Unit = synchronized(mutex) {
		data.containedChunks?.forEach { chunkKeyMap[it].remove(data) }
		data.containedChunks = state.coveredChunks
		data.containedChunks?.forEach { chunkKeyMap[it].add(data) }

		data.companion().updateChunks(data._id, state.coveredChunks)

		val saveFile = DeactivatedPlayerStarships.getSaveFile(data.bukkitWorld(), data)
		FileOutputStream(saveFile).use {
			state.writeToStream(it)
		}

		savedStateCache.put(data, Optional.of(state))
	}

	operator fun get(blockKey: Long): StarshipData? = blockKeyMap[blockKey]

	operator fun get(x: Int, y: Int, z: Int): StarshipData? = this[blockKey(x, y, z)]

	fun getInChunk(chunk: Chunk): List<StarshipData> {
		val chunkKey = chunk.chunkKey

		if (!chunkKeyMap.containsKey(chunkKey)) {
			return listOf()
		}

		return chunkKeyMap[chunkKey].toList()
	}

	fun getContaining(x: Int, y: Int, z: Int): StarshipData? {
		val blockKey = blockKey(x, y, z)
		val chunkKey = chunkKey(x shr 4, z shr 4)

		for (data: StarshipData in chunkKeyMap.get(chunkKey)) {
			val state = savedStateCache[data].orNull() ?: continue

			if (!state.blockMap.containsKey(blockKey)) {
				continue
			}

			return data
		}

		return null
	}

	fun getLockedContaining(x: Int, y: Int, z: Int): StarshipData? {
		val data = getContaining(x, y, z)

		if (data?.isLockActive() == false) {
			return null
		}

		return data
	}
}
