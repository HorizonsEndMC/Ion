package net.horizonsend.ion.server.features.multiblock.newer

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.subStringBetween
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.misc.MultiblockCommand
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockEntities.getMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockEntities.removeMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.SignMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
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

	/**
	 * Finds the first multiblock that can be detected at the position.
	 *
	 * If the block face provided is null, every block face will be checked
	 *
	 * @return the multiblock found, or null
	 **/
	private fun computeMultiblockAtLocation(
		world: World,
		x: Int,
		y: Int,
		z: Int,
		face: BlockFace?,
		loadChunks: Boolean = false,
		restrictedList: Collection<Multiblock> = MultiblockRegistration.getAllMultiblocks().values
	): Multiblock? {
		// Will only return null if not loaded and don't load chunks
		val originBlock = if (loadChunks) world.getBlockAt(x, y, z) else getBlockIfLoaded(world, x, y, z) ?: return null

		for (multiblock in restrictedList) {
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

	fun getMultiblock(sign: Sign, checkStructure: Boolean, loadChunks: Boolean): Multiblock? {
		if (!checkStructure) {
			return sign.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING)?.let {
				MultiblockRegistration.getByStorageName(it)
			}
		}

		val origin = sign.getMultiblockOrigin()
		return getMultiblock(sign.world, origin.x, origin.y, origin.z, sign.getFacing().oppositeFace, checkStructure, loadChunks)
	}

	/**
	 * Handle the setup and creation of the multiblock. Assumes structure has already been checked.
	 *
	 * @return if the multiblock could be created properly.
	 **/
	fun setMultiblock(detector: Player, world: World, x: Int, y: Int, z: Int, face: BlockFace, multiblock: Multiblock): Boolean {
		if (multiblock is SignMultiblock) {
			val signOffset = face.oppositeFace
			val signLoc = Vec3i(x, y, z).getRelative(signOffset)
			val sign = getBlockIfLoaded(world, signLoc.x, signLoc.y, signLoc.z)?.state as? Sign ?: return false

			Tasks.sync {
				sign.persistentDataContainer.set(
					NamespacedKeys.MULTIBLOCK,
					PersistentDataType.STRING,
					multiblock.javaClass.simpleName
				)

				sign.isWaxed = true

				multiblock.setupSign(detector, sign)

				sign.update(false, false)
			}
		}

		if (multiblock is EntityMultiblock<*>) {
			val present = getMultiblockEntity(world, x, y, z)

			if (present != null) {
				log.warn("Attempted to place multiblock entity where one was already present!")
			}

			val chunkX = x.shr(4)
			val chunkZ = z.shr(4)

			world.ion.getChunk(chunkX, chunkZ)?.let {
				it.region.launch { it.multiblockManager.addNewMultiblockEntity(multiblock, x, y, z, face) }
			}
		}

		return true
	}

	/**
	 * Remove this multiblock & entity, provided the multiblock origin
	 **/
	fun removeMultiblock(world: World, x: Int, y: Int, z: Int, facing: BlockFace): Multiblock? {
		val removed = removeMultiblockEntity(world, x, y, z)
		multiblockCache[world]?.invalidate(Vec3i(x, y, z) to facing)
		return removed?.multiblock
	}

	/**
	 * Handle the identification, detection and setup of the multiblock, if one is present at the location.
	 *
	 * @return the detected multiblock, if found
	 **/
	fun tryDetectMultiblock(player: Player, sign: Sign, loadChunks: Boolean = false): Multiblock? {
		val signLoc = Vec3i(sign.location)
		val world = sign.world

		val blankText = sign.front().line(0).plainText()
		val name = blankText.subStringBetween('[', ']')

		val direction = sign.getFacing().oppositeFace

		// Get the block that the sign is placed on
		val originBlock = signLoc.getRelative(direction, 1)
		val (x, y, z) = originBlock

		// Possible multiblocks from the sign
		val possible = MultiblockRegistration.getBySignName(name)

		if (possible.isEmpty()) return null

		val found = computeMultiblockAtLocation(
			world,
			x,
			y,
			z,
			null,
			loadChunks = loadChunks,
			restrictedList = possible
		)

		if (found == null) {
			player.userError("Improperly built $name. Make sure every block is correctly placed!")

			MultiblockCommand.setupCommand(player, sign, possible.first())

			return null
		}

		setMultiblock(player, world, x, y, z, direction, found)

		return found
	}

	@EventHandler
	fun onWorldUnload(event: WorldUnloadEvent) {
		val cache = multiblockCache.remove(event.world) ?: return
		cache.invalidateAll()
	}

	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND || event.action != Action.RIGHT_CLICK_BLOCK) return

		val clickedBlock = event.clickedBlock ?: return
		val sign = clickedBlock.state as? Sign ?: return

		if (sign.persistentDataContainer.has(NamespacedKeys.MULTIBLOCK)) return

		val result = tryDetectMultiblock(event.player, sign, false)

		if (result != null) {
			event.player.success("Detected new ${result.name}")
		}
	}

	@EventHandler
	fun onPlayerBreakBlock(event: BlockBreakEvent) {
		if (getBlockTypeSafe(event.block.world, event.block.x, event.block.y, event.block.z)?.isSign == false) return
		val sign = event.block.state as? Sign ?: return

		val origin = sign.getMultiblockOrigin()

		val removed = removeMultiblock(event.block.world, origin.x, origin.y, origin.z, sign.getFacing().oppositeFace) ?: return

		event.player.information("Destroyed ${removed.name}")
	}

	fun Sign.getMultiblockOrigin(): Block {
		return block.getRelative(getFacing().oppositeFace, 1)
	}
}