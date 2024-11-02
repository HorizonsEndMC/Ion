package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.type.misc.DecomposerMultiblock
import net.horizonsend.ion.server.features.ores.OldOreData
import net.horizonsend.ion.server.features.starship.isFlyable
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ItemDisplayContainer
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.Material.AIR
import org.bukkit.Material.GRAY_CONCRETE
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.joml.Vector3f

class DecomposeTask(
	private val entity: DecomposerMultiblock.DecomposerEntity,
	private val maxWidth: Int,
	private val maxHeight: Int,
	private val maxDepth: Int
) : BukkitRunnable() {
	private var totalBlocksBroken = 0

	private var currentBlock: Block = entity.getBlockRelative(1, 1, 1)

	private val displayItem = ItemStack(GRAY_CONCRETE)

	val rightDisplay = ItemDisplayContainer(
		entity.world,
		0.25f,
		entity.vec3i.toCenterVector(),
		entity.structureDirection.rightFace.direction,
		displayItem
	).apply { scale = Vector3f(0.25f, 0.25f, 1.0f) }

	val upDisplay = ItemDisplayContainer(
		entity.world,
		0.25f,
		entity.vec3i.toCenterVector(),
		BlockFace.UP.direction,
		displayItem
	).apply { scale = Vector3f(0.25f, 0.25f, 1.0f) }

	val forwardDisplay = ItemDisplayContainer(
		entity.world,
		0.25f,
		entity.vec3i.toCenterVector(),
		entity.structureDirection.direction,
		displayItem
	).apply { scale = Vector3f(0.25f, 0.25f, 1.0f) }

	override fun run() {
		try {
			if (breakStrip()) return
		} catch (exception: Exception) {
			entity.userManager.getUserPlayer()?.serverError("Decomposer encountered server error. Please contact staff.")
			exception.printStackTrace()
			cancel()
			return
		}

		cancel()
	}

	override fun cancel() {
		super.cancel()

		rightDisplay.remove()
		upDisplay.remove()
		forwardDisplay.remove()

		entity.userManager.getUserPlayer()?.information("Decomposer broke $totalBlocksBroken blocks.")
		entity.userManager.clear()
		entity.currentTask = null
	}

	/** returns whether it should be cancelled */
	private fun breakStrip(): Boolean {
		val player = entity.userManager.getUserPlayer() ?: return false

		if (!entity.isIntact(checkSign = true)) {
			player.userError("Decomposer destroyed.")
			return false
		}

		val storage = entity.getStorage() ?: return run {
			player.userError("Decomposer storage missing.")
			false
		}

		var power = entity.powerStorage.getPower()
		var iterationBroken = 0
		var success = true

		while (iterationBroken < DecomposerMultiblock.BLOCKS_PER_SECOND) {
			// If decompose task is cancelled
			if (isCancelled) break

			if (!currentBlock.location.isChunkLoaded) {
				moveForward()
				continue
			}

			if (currentBlock.type.isAir) {
				moveForward()
				continue
			}

			if (!BlockBreakEvent(currentBlock, player).callEvent()) {
				moveForward()
				continue
			}

			if (power < 10) {
				player.userError("Decomposer out of power!")
				success = false
				break
			}

			power -= 10

			val blockData = currentBlock.blockData
			val customBlock = CustomBlocks.getByBlockData(blockData)

			// get drops BEFORE breaking
			var drops: List<ItemStack> = currentBlock.drops.toList()

			var customOre = false
			OldOreData.entries.forEach { ore -> if (ore.blockData == (customBlock?.blockData ?: false)) customOre = true }

			if (customBlock != null && !customOre) drops = customBlock.drops.getDrops(null, false).toList()

			currentBlock.setType(AIR, false)

			if (isFlyable(blockData.nms)) {
				val remaining: HashMap<Int, ItemStack> = storage.addItem(*drops.toTypedArray())

				if (remaining.any()) {
					for (drop in drops) currentBlock.world.dropItemNaturally(entity.getSignLocation(), drop)

					player.userError("Decomposer out of space, dropping items and cancelling decomposition.")
					success = false
					break
				}
			}

			updateDisplays()
			totalBlocksBroken++
			iterationBroken++
		}

		entity.powerStorage.setPower(power)

		for (soundPlayer in entity.world.players) {
			if (soundPlayer.location.distance(currentBlock.location) >= maxWidth * 1.5) continue

			soundPlayer.playSound(
				soundPlayer.location,
				currentBlock.blockData.soundGroup.breakSound,
				SoundCategory.BLOCKS,
				0.5f,
				1.0f
			)
		}

		return success
	}

	private fun updateDisplays() {
		val block = currentBlock
		val origin = entity.getOrigin()
		val right = entity.structureDirection.rightFace

		// + 1 to get one block inside the frame
		val currentWidth = (right.modX * (block.x - origin.x)) + (right.modZ * (block.z - origin.z)) + 1
		val currentHeight = block.y - origin.y + 1
		val currentDepth = (entity.structureDirection.modX * (block.x - origin.x)) + (entity.structureDirection.modZ * (block.z - origin.z)) + 1

		val heightOffset = Vector(0, currentHeight, 0)
		val widthOffset = Vector(currentWidth * right.modX, 0, currentWidth * right.modZ)
		val depthOffset = Vector(currentWidth * right.modX, currentHeight, currentWidth * right.modZ)

		// Up display comes out of the width defining bar
		upDisplay.offset = widthOffset.toVector3f()
		upDisplay.scale = Vector3f(0f, 0f, currentDepth.toFloat())
		upDisplay.update()

		// Right display comes out of the vertical bar
		rightDisplay.offset = heightOffset.toVector3f()
		upDisplay.scale = Vector3f(0f, 0f, currentDepth.toFloat())
		upDisplay.update()

		// Depth display comes from the intersection point of the 2 bars
		forwardDisplay.offset = depthOffset.toVector3f()
		forwardDisplay.scale = Vector3f(0f, 0f, currentDepth.toFloat())
		forwardDisplay.update()
	}

	private fun moveForward(): Boolean {
		val next = getNextBlock(currentBlock) ?: return false
		currentBlock = next
		return true
	}

	private fun getNextBlock(block: Block): Block? {
		val origin = entity.getOrigin()
		val right = entity.structureDirection.rightFace

		// + 1 to get one block inside the frame
		val currentWidth = (right.modX * (block.x - origin.x)) + (right.modZ * (block.z - origin.z)) + 1
		val currentHeight = block.y - origin.y + 1
		val currentDepth = (entity.structureDirection.modX * (block.x - origin.x)) + (entity.structureDirection.modZ * (block.z - origin.z)) + 1

		// If possible move forward
		return if (currentDepth < maxDepth) {
			// If can't move forward, start next row
			entity.getBlockRelative(currentWidth, currentHeight, currentDepth + 1)
		} else if (currentHeight < maxHeight) {
			// If can't move up, start new column
			entity.getBlockRelative(currentWidth, currentHeight + 1, 1)
		} else if (currentWidth < maxWidth) {
			// Try move right, if can't, it's done
			entity.getBlockRelative(currentWidth + 1, 1, 1)
		} else {
			cancel()
			null
		}
	}
}
