package net.horizonsend.ion.server.features.multiblock.newer

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.SignMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldUnloadEvent
import java.util.Optional
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

object MultiblockAccess : IonServerComponent() {
	private val multiblockCache: MutableMap<World, LoadingCache<Pair<Vec3i, BlockFace>, Optional<Multiblock>>> = mutableMapOf()

	/**
	 * Access or compute the multiblock at a world position
	 *
	 * The position provided should be the origin position, not the sign position
	 **/
	fun getCachedMultiblock(world: World, x: Int, y: Int, z: Int, face: BlockFace): Multiblock? {
		return multiblockCache.getOrPut(world) {
			CacheBuilder
				.newBuilder()
				.weakKeys()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.build(CacheLoader.from { (location, face) ->
					return@from Optional.ofNullable(computeMultiblockAtLocation(world, location.x, location.y, location.z, face))
				})
		}[Vec3i(x, y, z) to face].getOrNull()
	}

	private fun computeMultiblockAtLocation(world: World, x: Int, y: Int, z: Int, face: BlockFace?, loadChunks: Boolean = false): Multiblock? {
		// Will only return null if not loaded and don't load chunks
		val originBlock = if (loadChunks) world.getBlockAt(x, y, z) else getBlockIfLoaded(world, x, y, z) ?: return null

		for ((_, multiblock) in MultiblockRegistration.getAllMultiblocks()) {
			// If provided a face, check only it
			if (face != null) {
				if (!multiblock.blockMatchesStructure(originBlock, face, loadChunks, false)) continue
			}
			// If no face is provided, e.g. no sign available to reference, check every side
			else if (CARDINAL_BLOCK_FACES.none {
				multiblock.blockMatchesStructure(originBlock, it, loadChunks, false)
			}) continue

			return multiblock
		}

		return null
	}

	/**
	 * Attempt to get the multiblock at the location provided
	 *
	 * The position provided should be the origin position, not the sign position
	 **/
	fun getMultiblock(world: World, x: Int, y: Int, z: Int, face: BlockFace, checkStructure: Boolean, loadChunks: Boolean = false): Multiblock? {
		// Will only return null if not loaded and don't load chunks
		val originBlock = if (loadChunks) world.getBlockAt(x, y, z) else getBlockIfLoaded(world, x, y, z) ?: return null

		val cached = getCachedMultiblock(world, x, y, z, face)

		if (cached != null) {
			if (!checkStructure) return cached

			return cached.takeIf {
				cached.blockMatchesStructure(originBlock, face, loadChunks, false)
			}
		}

		if (!loadChunks || !checkStructure) return null

		// If the cache didn't compute it, there's a chance it wasn't loaded. In that case, compute again and load chunks, if requested.
		return computeMultiblockAtLocation(world, x, y, z, face, true)
	}

	fun setMultiblock(world: World, x: Int, y: Int, z: Int, face: BlockFace, multiblock: Multiblock): Boolean {
		if (multiblock is SignMultiblock) {
			val signOffset = face.oppositeFace
			val signLoc = Vec3i(x, y, z).getRelative(signOffset)
			val sign = getBlockIfLoaded(world, signLoc.x, signLoc.y, signLoc.z)?.state as? Sign ?: return false


		}

		if (multiblock is EntityMultiblock<*>) {

		}

		return true
	}

	/**
	 * Remove this multiblock & entity, provided the multiblock origin
	 **/
	fun removeMultiblock(world: World, x: Int, y: Int, z: Int, facing: BlockFace) {
		//TODO remove multiblock entity
		multiblockCache[world]?.invalidate(Vec3i(x, y, z) to facing)
	}

	fun tryDetectMultiblock(x: Int, y: Int, z: Int) {

	}

	fun tryDetectMultiblock(x: Int, y: Int, z: Int) {

	}

	@EventHandler
	fun onWorldUnload(event: WorldUnloadEvent) {
		val cache = multiblockCache.remove(event.world) ?: return
		cache.invalidateAll()
	}
}
