package net.starlegacy.feature.misc

import java.util.UUID
import net.horizonsend.ion.core.feedback.FeedbackType
import net.horizonsend.ion.core.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.ores.Ore
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.misc.DecomposerMultiblock
import net.starlegacy.feature.starship.isFlyable
import net.starlegacy.util.getBlockIfLoaded
import net.starlegacy.util.nms
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class DecomposeTask(
	private val signLoc: Location,
	private val width: Int,
	private val height: Int,
	private val length: Int,
	private val origin: Location,
	private val right: BlockFace,
	private val up: BlockFace,
	private val forward: BlockFace,
	private val playerID: UUID,
	private val multiblock: DecomposerMultiblock,
) : BukkitRunnable() {
	private var offsetRight = 0
	private var blocksBroken = 0

	override fun run() {
		try {
			if (breakStrip()) {
				return
			}
		} catch (exception: Exception) {
			exception.printStackTrace()
			cancel()
			return
		}

		cancel()

		Bukkit.getPlayer(playerID)?.sendFeedbackMessage(FeedbackType.INFORMATION, "&7Decomposer broke &c${blocksBroken} blocks.")
	}

	override fun cancel() {
		super.cancel()

		Decomposers.busySigns.remove(signLoc)
	}

	private fun breakStrip(): Boolean {
		if (offsetRight >= width) {
			return false
		}

		val player = Bukkit.getPlayer(playerID)
			?: return false

		val signBlock = getBlockIfLoaded(signLoc.world, signLoc.blockX, signLoc.blockY, signLoc.blockZ)
			?: return false
		val sign = signBlock.state as? Sign
			?: return false

		if (!multiblock.signMatchesStructure(sign, loadChunks = false)) {
			return false
		}

		val storage = DecomposerMultiblock.getStorage(sign)

		val power = PowerMachines.getPower(sign, fast = true)

		for (offsetUp: Int in 0 until height) {
			for (offsetForward: Int in 0 until length) {
				val blockPosition = origin.clone()
					.add(right.direction.multiply(offsetRight))
					.add(up.direction.multiply(offsetUp))
					.add(forward.direction.multiply(offsetForward))
					.toBlockLocation()

				if (!blockPosition.isChunkLoaded) {
					continue
				}

				val block = blockPosition.block

				val blockData = block.blockData

				if (blockData.material.isAir) {
					continue
				}

				if (!BlockBreakEvent(block, player).callEvent()) {
					continue
				}

				if (power < 10) {
					player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Decomposer out of power!")
					return false
				}

				PowerMachines.removePower(sign, 10)

				// get drops BEFORE breaking
				var drops: Collection<ItemStack> = block.drops

				val customBlock = CustomBlocks[block] != null
				var customOre = false
				Ore.values().forEach { Ore -> if (Ore.blockData == (CustomBlocks[block]?.blockData ?: false)) customOre = true }

				if (customBlock && !customOre) {
					drops = CustomBlocks[block]?.getDrops()?.toList()!!
				}

				block.setType(Material.AIR, false)

				blocksBroken++

				if (isFlyable(blockData.nms)) {
					val remaining: HashMap<Int, ItemStack> = storage.addItem(*drops.toTypedArray())
					if (remaining.any()) {
						for (drop in drops) {
							sign.world.dropItemNaturally(sign.location.toCenterLocation(), drop)
						}

						player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Decomposer out of space, dropping items and cancelling decomposition.")
						return false
					}
				}
			}
		}

		offsetRight++
		return true
	}
}
