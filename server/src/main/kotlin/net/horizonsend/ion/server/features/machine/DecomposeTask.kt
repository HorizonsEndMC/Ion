package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.entity.task.MultiblockEntityTask
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock.Companion.formatProgress
import net.horizonsend.ion.server.features.multiblock.type.misc.DecomposerMultiblock
import net.horizonsend.ion.server.features.ores.OldOreData
import net.horizonsend.ion.server.features.starship.isFlyable
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Material.AIR
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class DecomposeTask(
	override val taskEntity: DecomposerMultiblock.DecomposerEntity,
	private val maxWidth: Int,
	private val maxHeight: Int,
	private val maxDepth: Int
) : MultiblockEntityTask<DecomposerMultiblock.DecomposerEntity>, Iterable<Vec3i> {
	private var totalBlocksBroken = 0
	private var totalBlocksSkiped = 0

	private val totalVolume = maxWidth * maxDepth * maxHeight

	override fun disable() {
		taskEntity.stopTask()
		taskEntity.userManager.clear()
	}

	override var isDisabled: Boolean = false

	override fun onDisable() {
		isDisabled = true
		taskEntity.userManager.getUserPlayer()?.information("Decomposer broke $totalBlocksBroken blocks.")
		taskEntity.userManager.clear()
	}

	override fun tick() {
		val player = taskEntity.userManager.getUserPlayer()

		if (player == null) {
			disable()
			return
		}

		if (!taskEntity.isIntact(checkSign = false)) {
			taskEntity.setStatus(Component.text("Not Intact", NamedTextColor.RED))
			player.userError("Decomposer destroyed.")
			disable()
			return
		}

		val storage = taskEntity.getStorage()

		if (storage == null) {
			taskEntity.setStatus(Component.text("Storage missing.", NamedTextColor.RED))
			player.userError("Decomposer storage missing.")
			disable()
			return
		}

		var power = taskEntity.powerStorage.getPower()

		Tasks.async {
			val toBreak = mutableListOf<Block>()

			for (position in this) {
				val realPosition = getRealCoordinate(position)
				val block = getBlockIfLoaded(taskEntity.world, realPosition.x, realPosition.y, realPosition.z)

				if (block == null) {
					totalBlocksSkiped++
					continue
				}

				if (block.type.isAir) {
					totalBlocksSkiped++
					continue
				}

				if (power < 10) {
					taskEntity.setStatus(Component.text("Out of Power", NamedTextColor.RED))
					player.userError("Decomposer out of power!")
					break
				}

				power -= 10
				toBreak.add(block)
				if (toBreak.size >= DecomposerMultiblock.BLOCKS_PER_SECOND) break
			}

			taskEntity.setStatus(formatPercent())
			taskEntity.powerStorage.setPower(power)

			if (toBreak.isEmpty()) {
				taskEntity.setStatus(Component.text("Nothing to Break", NamedTextColor.RED))
				player.userError("Decomposer had nothing to break.")
				disable()
				return@async
			}

			Tasks.sync {
				if (!toBreak.all { breakBlock(it, storage, player) }) {
					disable()
					return@sync
				}
			}
		}

//		for (soundPlayer in entity.world.players) {
//			if (soundPlayer.location.distance(getRealCoordinate().toLocation(entity.world)) >= maxWidth * 1.5) continue
//
//			soundPlayer.playSound(
//				soundPlayer.location,
//				currentBlock.blockData.soundGroup.breakSound,
//				SoundCategory.BLOCKS,
//				0.5f,
//				1.0f
//			)
//		}
	}

	private fun formatPercent(): Component {
		return formatProgress(NamedTextColor.WHITE, (totalBlocksBroken + totalBlocksSkiped) / totalVolume.toDouble())
	}

	private fun breakBlock(block: Block, storage: Inventory, player: Player): Boolean {
		val event = BlockBreakEvent(block, player)
		event.isDropItems = false

		if (!event.callEvent()) {
			totalBlocksSkiped++
			return true
		}

		val blockData = block.blockData
		val customBlock = CustomBlocks.getByBlockData(blockData)

		// get drops BEFORE breaking
		var drops: List<ItemStack> = block.drops.toList()

		val state = block.state
		if (state is InventoryHolder) {
			drops = drops.plus(state.inventory.contents.filterNotNull())
			state.inventory.clear()
		}

		if (block.type == Material.END_PORTAL_FRAME) {
			drops = listOf(ItemStack(Material.END_PORTAL_FRAME))
		}

		var customOre = false
		//TODO fix this bad code
		OldOreData.entries.forEach { ore -> if (ore.blockData == (customBlock?.blockData ?: false)) customOre = true }

		if (customBlock != null) {
			drops = if (!customOre) {
				customBlock.drops.getDrops(null, false).toList()
			} else {
				listOf()
			}
		}

		block.setType(AIR, false)

		totalBlocksBroken++

		if (isFlyable(blockData.nms)) {
			val remaining: HashMap<Int, ItemStack> = storage.addItem(*drops.toTypedArray())

			if (remaining.any()) {
				for (drop in remaining) block.world.dropItemNaturally(taskEntity.getSignLocation(), drop.value)

				taskEntity.setStatus(Component.text("Out of space.", NamedTextColor.RED))
				player.userError("Decomposer out of space, dropping items and cancelling decomposition.")
				return false
			}
		}

		return true
	}

	private fun getRealCoordinate(offset: Vec3i) = taskEntity.getPosRelative(right = offset.x + 1, up = offset.y + 1, forward = offset.z + 1)

	private var currentPosition: Vec3i = Vec3i(0, 0, -1)

	private val iterator = object : Iterator<Vec3i> {
		var hasNext = true
		override fun hasNext(): Boolean = hasNext

		override fun next(): Vec3i {
			if (currentPosition.z + 1 < maxDepth) {
				val new = Vec3i(currentPosition.x, currentPosition.y, currentPosition.z + 1)
				currentPosition = new
				return currentPosition
			}

			else if (currentPosition.y + 1 < maxHeight) {
				val new = Vec3i(currentPosition.x, currentPosition.y + 1, 0)
				currentPosition = new
				return currentPosition
			}

			else if (currentPosition.x + 1 < maxWidth) {
				val new =  Vec3i(currentPosition.x + 1, 0, 0)
				currentPosition = new
				return currentPosition
			}

			else {
				hasNext = false
				return Vec3i(maxWidth, maxHeight, maxDepth)
			}
		}
	}

	override fun iterator(): Iterator<Vec3i> {
		return iterator
	}
}
