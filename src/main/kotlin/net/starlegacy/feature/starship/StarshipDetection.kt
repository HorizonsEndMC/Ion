package net.starlegacy.feature.starship

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.min
import kotlin.math.sqrt
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.chunkKey
import net.starlegacy.util.getBlockDataSafe
import net.starlegacy.util.isConcrete
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask

object StarshipDetection : SLComponent() {
	class DetectionFailedException(message: String) : Exception(message)

	private val activeTasks = ConcurrentHashMap<Player, BukkitTask>()

	override fun onEnable() {
		subscribe<PlayerQuitEvent> { event -> cancel(event.player) }
	}

	/** Returns true if a job was present and cancelled, false if absent or already cancelled */
	private fun cancel(player: Player): Boolean = activeTasks.remove(player)?.cancel() != null

	override fun onDisable() {
		log.info("Cancelling tasks...")
		for ((_, job) in activeTasks) {
			job.cancel()
		}
		activeTasks.clear()
		log.info("  -> Cancelled")
	}
	//endregion

	fun detectNewState(data: PlayerStarshipData): PlayerStarshipState {
		val world = data.bukkitWorld()
/*
        val forbiddenBlocks = ForbiddenBlocks.getForbiddenBlocks(world)
*/
		val computerLocation = Vec3i(data.blockKey)

		// blocks that were accepted
		val blocks = mutableListOf<Vec3i>()
		val blockTypes = Long2ObjectOpenHashMap<BlockData>()

		// blocks that are pending checking
		val queue = Stack<Long>()

		// blocks that were already checked and should not be detected twice
		val visited = mutableSetOf<Long>()

		/** Various counters */
		// used for avoiding detecting blocks below landing gears
		var lowestY = computerLocation.y

		var carbyne = 0
		var containers = 0
		var stickyPistons = 0

		// Jumpstart the queue by adding the origin block
		val computerKey = computerLocation.toBlockKey()
		visited.add(computerKey)
		queue.push(computerKey)

		var minX: Int? = null
		var minY: Int? = null
		var minZ: Int? = null

		var maxX: Int? = null
		var maxY: Int? = null
		var maxZ: Int? = null

		val start = System.nanoTime()

		while (!queue.isEmpty()) {
			if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(30)) {
				throw DetectionFailedException("Timed out!")
			}

			val key = queue.pop()
			val x = blockKeyX(key)
			val y = blockKeyY(key)
			val z = blockKeyZ(key)

			val blockData = getBlockDataSafe(world, x, y, z)
			// Do not allow checking ships larger than render distance.
			// The type being null usually means the chunk is unloaded.
				?: throw DetectionFailedException("The blocks went beyond loaded chunks!")

			val material = blockData.material

			// Prevent landing gear extensions from connecting to the ground beneath the ship
			if (material == Material.PISTON && (blockData as Directional).facing == BlockFace.DOWN && lowestY > y) {
				continue
			}

			// Don't allow blocks that are not flown

			if (!FLYABLE_BLOCKS.contains(material)) {
				continue
			}

/*
            // Don't allow blocks that have been added to the forbidden blocks list
            if (forbiddenBlocks.contains(key)) {
                continue
            }
*/

			//--------------------------------------------------------------------
			// Past this point, the block has been validated and will be detected
			//--------------------------------------------------------------------

			// If this block's Y level is lower than lowestY, update lowestY
			lowestY = min(lowestY, y)

			// Add the location to the list of blocks that'll be set on the starships
			blockTypes[key] = blockData

			// Update the various counters
			when {
				material.isConcrete -> carbyne++
				isInventory(material) -> containers++
				material == Material.STICKY_PISTON -> stickyPistons++
			}

			if (material == Material.CHEST || material == Material.TRAPPED_CHEST || material == Material.BARREL) {
				containers += 2
			}

			// Detect adjacent blocks
			for (offsetX in -1..1) {
				for (offsetY in -1..1) {
					for (offsetZ in -1..1) {
						val adjacentX = offsetX + x
						val adjacentY = offsetY + y
						val adjacentZ = offsetZ + z

						// Ensure it's a valid Y-level before adding it to the queue
						if (adjacentY < 0 || adjacentY > 255) {
							continue
						}

						val key1 = blockKey(adjacentX, adjacentY, adjacentZ)
						if (visited.add(key1)) {
							queue.push(key1)
						}
					}
				}
			}

			if (minX == null || minX > x) minX = x
			if (minY == null || minY > y) minY = y
			if (minZ == null || minZ > z) minZ = z
			if (maxX == null || maxX < x) maxX = x
			if (maxY == null || maxY < y) maxY = y
			if (maxZ == null || maxZ < z) maxZ = z
		}

		val type = data.type

		// Validate the size
		val size = blockTypes.size

		if (size < type.minSize) {
			throw DetectionFailedException(
				"The ship's too small! Minimum size for '${type.displayName}' is ${type.minSize}, " +
					"but there were only $size blocks. Consider changing the ship's class."
			)
		}

		if (size > type.maxSize) {
			throw DetectionFailedException(
				"The ship's too big! Maximum size for '${type.displayName}' is ${type.maxSize}, " +
					"but there were $size blocks. Consider changing the ship's class."
			)
		}

		val carbynePercent: Double = carbyne.toDouble() / size.toDouble()
		if (carbynePercent < 0.3) {
			throw DetectionFailedException(
				"All ships require at least 30% carbyne (concrete) blocks in order to fly. Current %: ${carbynePercent * 100}"
			)
		}

		val containerPercent: Double = containers.toDouble() / size.toDouble()
		val maxContainerPercent = type.containerPercent
		if (containerPercent > maxContainerPercent) {
			throw DetectionFailedException(
				"'${type.displayName}' can't fly with more than more than ${maxContainerPercent * 100}% containers. " +
					"Current %: ${containerPercent * 100}"
			)
		}

		val maxStickyPistons: Int = (min(0.015 * size, sqrt(size.toDouble())) * type.crateLimitMultiplier).toInt()
		if (stickyPistons > maxStickyPistons) {
			throw DetectionFailedException(
				"Your ship can only fit $maxStickyPistons sticky pistons but it has $stickyPistons"
			)
		}

		/*// Allow listeners to cancel the detection
		if (!StarshipDetectEvent(player, computer, world, blockTypes.keys).callEvent()) {
				throw StarshipDetection.DetectionFailedException(
						"Detection cancelled"
				)
		}*/

		val coveredChunks = LongOpenHashSet()

		for (block in blocks) {
			coveredChunks += chunkKey(block.x shr 4, block.z shr 4)
		}

		checkNotNull(minX)
		checkNotNull(minY)
		checkNotNull(minZ)
		checkNotNull(maxX)
		checkNotNull(maxY)
		checkNotNull(maxZ)

		return PlayerStarshipState(coveredChunks, blockTypes, Vec3i(minX, minY, minZ), Vec3i(maxX, maxY, maxZ))
	}

	fun isInventory(material: Material): Boolean {
		return when (material) {
			Material.CHEST,
			Material.TRAPPED_CHEST,
			Material.HOPPER,
			Material.DISPENSER,
			Material.DROPPER -> true
			else -> false
		}
	}
}
