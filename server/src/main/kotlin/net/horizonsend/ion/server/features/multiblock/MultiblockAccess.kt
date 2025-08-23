package net.horizonsend.ion.server.features.multiblock

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.misc.MultiblockCommand
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities.getMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities.removeMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.kyori.adventure.audience.Audience
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.Optional
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

object MultiblockAccess : IonServerComponent() {
	private val multiblockOriginCache: MutableMap<World, LoadingCache<Pair<Vec3i, BlockFace>, Optional<Multiblock>>> = mutableMapOf()
	/** Provides the means to access the multiblock origin cache from the sign position */
	private data class StructureLocator(val structureDirection: BlockFace, val structureOrigin: Vec3i)
	private val multiblockSignCache: MutableMap<World, LoadingCache<Sign, Optional<StructureLocator>>> = mutableMapOf()

	/**
	 * Access or compute the multiblock at a world position
	 * The position provided should be the origin position, not the sign position
	 **/
	private fun getCachedMultiblock(world: World, x: Int, y: Int, z: Int, face: BlockFace): Multiblock? {
		return multiblockOriginCache.getOrPut(world) {
			CacheBuilder
				.newBuilder()
				.weakKeys()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.build(CacheLoader.from { (location, face) ->
					return@from Optional.ofNullable(computeMultiblockFromOrigin(world, location.x, location.y, location.z, face))
				})
		}[Vec3i(x, y, z) to face].getOrNull()
	}

	/**
	 * Access or compute the multiblock at a world position
	 * The position provided should be the origin position, not the sign position
	 **/
	private fun getCachedMultiblock(sign: Sign): Multiblock? {
		val locator = multiblockSignCache.getOrPut(sign.world) {
			CacheBuilder
				.newBuilder()
				.weakKeys()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.build(CacheLoader.from { sign ->
					val multiblock = getStored(sign, false) ?: return@from Optional.empty()
					return@from Optional.of(StructureLocator(sign.getFacing().oppositeFace, multiblock.getOriginLocation(sign)))
				})
		}[sign].getOrNull() ?: return null

		return getCachedMultiblock(sign.world, locator.structureOrigin.x, locator.structureOrigin.y, locator.structureOrigin.z, locator.structureDirection)
	}

	/**
	 * Quickly gets the multiblock type stored in the sign. Does not check for structure
	 **/
	fun getFast(sign: Sign): Multiblock? {
		// Check for sign centered multiblocks first
		getCachedMultiblock(sign)?.let { return it }

		val cached = getCachedMultiblock(sign)
		if (cached != null) return cached

		return getStored(sign, false)
	}

	fun getStored(sign: Sign, checkStructure: Boolean = true): Multiblock? {
		val value = sign.persistentDataContainer.get(MULTIBLOCK, STRING) ?: return null

		val stored = MultiblockRegistration.getByStorageName(value)
		if (!checkStructure) return stored

		return stored?.takeIf { it.signMatchesStructure(sign, loadChunks = false, particles = false) }
	}

	/**
	 * Finds the first multiblock that can be detected at the position.
	 *
	 * If the block face provided is null, every block face will be checked
	 *
	 * @return the multiblock found, or null
	 **/
	private fun computeMultiblockFromSign(
		sign: Sign,
		loadChunks: Boolean = false,
		restrictedList: Collection<Multiblock> = MultiblockRegistration.getAllMultiblocks()
	): Multiblock? {
		for (multiblock in restrictedList) {
			val (originX, originY, originZ) = multiblock.getOriginLocation(sign)

			// Will only return null if not loaded and don't load chunks
			val originBlock = if (loadChunks) sign.world.getBlockAt(originX, originY, originZ) else
				getBlockIfLoaded(sign.world, originX, originY, originZ) ?: return null

			// If provided a face, check only it
			if (!multiblock.shape.signCentered) {
				if (!multiblock.blockMatchesStructure(originBlock, sign.getFacing().oppositeFace, loadChunks, false)) continue
			}
			// If turret
			else if (CARDINAL_BLOCK_FACES.none {
				multiblock.blockMatchesStructure(originBlock, it, loadChunks, false)
			}) continue

			return multiblock
		}

		return null
	}

	/**
	 * Finds the first multiblock that can be detected at the position.
	 *
	 * If the block face provided is null, every block face will be checked
	 *
	 * @return the multiblock found, or null
	 **/
	private fun computeMultiblockFromOrigin(
		world: World,
		x: Int,
		y: Int,
		z: Int,
		face: BlockFace?,
		loadChunks: Boolean = false,
		restrictedList: Collection<Multiblock> = MultiblockRegistration.getAllMultiblocks()
	): Multiblock? {
		// Will only return null if not loaded and don't load chunks
		val originBlock = if (loadChunks) world.getBlockAt(x, y, z) else getBlockIfLoaded(world, x, y, z) ?: return null

		for (multiblock in restrictedList) {
			// If provided a face, check only it
			if (face != null && !multiblock.shape.signCentered) {
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
	 * Handle the setup and creation of the multiblock. Assumes structure has already been checked.
	 *
	 * @return if the multiblock could be created properly.
	 **/
	fun setupMultiblock(detector: Player?, sign: Sign, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace, multiblock: Multiblock): Boolean {
		Tasks.sync {
			sign.persistentDataContainer.set(
				MULTIBLOCK,
				STRING,
				multiblock.javaClass.simpleName
			)

			sign.isWaxed = true

			if (detector != null) multiblock.setupSign(detector, sign)

			sign.update(false, false)
		}

		if (multiblock is EntityMultiblock<*>) {
			val present = getMultiblockEntity(world, x, y, z)

			if (present != null) {
				log.warn("Attempted to place multiblock entity where one was already present!")
			}

			val chunkX = x.shr(4)
			val chunkZ = z.shr(4)

			world.ion.getChunk(chunkX, chunkZ)?.multiblockManager?.handleNewMultiblockEntity(multiblock, x, y, z, structureDirection)
		}

		return true
	}

	/**
	 * Remove this multiblock & entity, provided the multiblock origin
	 **/
	private fun removeMultiblock(world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): Multiblock? {
		val existing = getMultiblockEntity(world, x, y, z)
		// Ensure that the removal additional signs placed on the sides of an origin don't remove the entity
		if (existing?.structureDirection != structureDirection) return null

		val removed = removeMultiblockEntity(world, x, y, z)
		multiblockOriginCache[world]?.invalidate(Vec3i(x, y, z) to structureDirection)
		return removed?.multiblock
	}

	/**
	 * Handle the identification, detection and setup of the multiblock, if one is present at the location.
	 *
	 * @return the detected multiblock, if found
	 **/
	fun tryDetectMultiblock(player: Player, sign: Sign, loadChunks: Boolean = false): Multiblock? {
		val world = sign.world

		val possibleMultiblocks = MultiblockRegistration
			.getAllMultiblocks()
			.filter { it.matchesUndetectedSign(sign) }

		if (possibleMultiblocks.isEmpty()) return null

		val direction = sign.getFacing().oppositeFace

		// Get the block that the sign is placed on
		val originBlock = MultiblockEntity.getOriginFromSign(sign)

		val found = computeMultiblockFromSign(
			sign,
			loadChunks = loadChunks,
			restrictedList = possibleMultiblocks
		)

		val named = possibleMultiblocks.first()

		if (found == null) {
			player.userError("Improperly built ${named.name}. Make sure every block is correctly placed!")

			MultiblockCommand.setupCommand(player, sign, named)

			return null
		}

		setupMultiblock(player, sign, world, originBlock.x, originBlock.y, originBlock.z, direction, found)

		return found
	}

	@EventHandler
	fun onWorldUnload(event: WorldUnloadEvent) {
		val originCache = multiblockOriginCache.remove(event.world) ?: return
		originCache.invalidateAll()
		val signCache = multiblockSignCache.remove(event.world) ?: return
		signCache.invalidateAll()
	}

	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND || (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.LEFT_CLICK_BLOCK)) return

		val clickedBlock = event.clickedBlock ?: return
		val sign = clickedBlock.state as? Sign ?: return

		if (sign.persistentDataContainer.has(MULTIBLOCK)) {
			checkInteractable(sign, event)
			return
		}

		val result = tryDetectMultiblock(event.player, sign, loadChunks = false) ?: return

		event.player.success("Detected new ${result.name}")
	}

	private fun checkInteractable(sign: Sign, event: PlayerInteractEvent) {
		// Quick check
		val multiblockType = getStored(sign)
		if (multiblockType !is InteractableMultiblock) return

		// Check structure
		val origin = multiblockType.getOriginBlock(sign)
		if (!multiblockType.blockMatchesStructure(origin, sign.getFacing().oppositeFace)) return

		multiblockType.onSignInteract(sign, event.player, event)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerBreakBlock(event: BlockBreakEvent) {
		if (event.isCancelled) return

		if (getBlockTypeSafe(event.block.world, event.block.x, event.block.y, event.block.z)?.isSign == false) {
			// If this is not a sign, it may support a sign
			checkSurroundingBlocks(event.block, event.player)
			return
		}

		val sign = event.block.state as? Sign ?: return

		val origin = MultiblockEntity.getOriginFromSign(sign)

		val removed = removeMultiblock(event.block.world, origin.x, origin.y, origin.z, sign.getFacing().oppositeFace) ?: return

		event.player.information("Destroyed ${removed.name}")
	}

	private fun checkSurroundingBlocks(brokenBlock: Block, audience: Audience) {
		for (face in ADJACENT_BLOCK_FACES) {
			val positon = brokenBlock.getRelativeIfLoaded(face) ?: continue
			val blockData = getBlockDataSafe(brokenBlock.world, positon.x, positon.y, positon.z) ?: continue

			if (blockData is WallSign) Tasks.sync {
				val postTickData = getBlockDataSafe(brokenBlock.world, positon.x, positon.y, positon.z) ?: return@sync
				if (!postTickData.material.isAir) return@sync

				val origin = MultiblockEntity.getOriginFromSignData(positon, blockData)
				val removed = removeMultiblock(brokenBlock.world, origin.x, origin.y, origin.z, blockData.facing.oppositeFace) ?: return@sync

				audience.information("Destroyed ${removed.name}")
			}
		}
	}
}
