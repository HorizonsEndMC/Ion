package net.starlegacy.feature.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.legacy.ores.Ore
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.misc.DecomposerMultiblock
import net.starlegacy.feature.multiblock.misc.DecomposerMultiblock.busySigns
import net.starlegacy.feature.starship.isFlyable
import net.starlegacy.util.getBlockIfLoaded
import net.starlegacy.util.nms
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

/**
 * @param width Iterated first,
 * @param startWidthOffset Start offset down width from 0.
 * @param height Iterated second,
 * @param length Iterated third,
 * @param right direction of width
 * @param up direction up height
 * @param forward direction of length
 * */
class DecomposeTask(
	private val signLoc: Location,
	private val width: Int,
	private var startWidthOffset: Int,
	private val height: Int,
	private val length: Int,
	private val origin: Location,
	private val right: BlockFace,
	private val up: BlockFace,
	private val forward: BlockFace,
	private val playerID: UUID,
	private val multiblock: DecomposerMultiblock
) : BukkitRunnable() {
	private var blocksBroken = 0

	override fun run() {
		try {
			if (breakStrip()) {
				return
			}
		} catch (exception: Exception) {
			Bukkit.getPlayer(playerID)
				?.serverError(
					"Decomposer encountered server error. Please contact staff."
				)
			exception.printStackTrace()
			cancel()
			return
		}

		cancel()
	}

	override fun cancel() {
		super.cancel()

		busySigns.remove(signLoc)
		Bukkit.getPlayer(playerID)
			?.information("Decomposer broke $blocksBroken blocks.")
	}

	private fun breakStrip(): Boolean {
		if (startWidthOffset >= width) return false

		val player = Bukkit.getPlayer(playerID) ?: return false

		val signBlock = getBlockIfLoaded(signLoc.world, signLoc.blockX, signLoc.blockY, signLoc.blockZ) ?: return false
		val sign = signBlock.state as? Sign ?: return false

		if (!multiblock.signMatchesStructure(sign, loadChunks = false)) {
			return false
		}

		val storage = DecomposerMultiblock.getStorage(sign)
		val power = PowerMachines.getPower(sign, fast = true)

		var firstBlock: BlockData? = null

		for (offsetUp: Int in 0 until height) {
			for (offsetForward: Int in 0 until length) {
				val newLocation = origin.clone()
					.add(right.direction.multiply(startWidthOffset))
					.add(up.direction.multiply(offsetUp))
					.add(forward.direction.multiply(offsetForward))
					.toBlockLocation()

				if (!newLocation.isChunkLoaded) {
					continue
				}

				val block = newLocation.block
				val blockData = block.blockData

				if (blockData.material.isAir) continue

				if (!BlockBreakEvent(block, player).callEvent()) continue

				if (firstBlock == null) { firstBlock = blockData }

				if (power < 10) {
					player.userError("Decomposer out of power!")
					return false
				}

				PowerMachines.removePower(sign, 10)

				// get drops BEFORE breaking
				var drops: Collection<ItemStack> = block.drops

				val customBlock = CustomBlocks[block] != null
				var customOre = false
				Ore.values().forEach { Ore -> if (Ore.blockData == (CustomBlocks[block]?.blockData ?: false)) customOre = true }

				if (customBlock && !customOre) drops = CustomBlocks[block]?.getDrops()?.toList()!!

				block.setType(Material.AIR, false)

				blocksBroken++

				if (isFlyable(blockData.nms)) {
					val remaining: HashMap<Int, ItemStack> = storage.addItem(*drops.toTypedArray())
					if (remaining.any()) {
						for (drop in drops) {
							sign.world.dropItemNaturally(sign.location.toCenterLocation(), drop)
						}

						player.userError(
							"Decomposer out of space, dropping items and cancelling decomposition."
						)
						return false
					}
				}
			}
		}

		firstBlock?.let {
			val stripCenter: Location = origin.clone()
				.add(right.direction.multiply(startWidthOffset))
				.add(up.direction.multiply(height / 2.0))
				.add(forward.direction.multiply(length / 2.0))
				.toBlockLocation()

			for (soundPlayer in origin.world.players) {
				if (soundPlayer.location.distance(stripCenter) >= width * 1.5) continue
				soundPlayer.playSound(
					soundPlayer.location,
					firstBlock.soundGroup.breakSound,
					SoundCategory.BLOCKS,
					0.5f,
					1.0f
				)
			}
		}

		startWidthOffset++

		return true
	}
}
