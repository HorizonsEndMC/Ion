package net.starlegacy.feature.multiblock

import net.starlegacy.util.colorize
import net.starlegacy.util.getBlockIfLoaded
import net.starlegacy.util.getFacing
import net.starlegacy.util.isValidYLevel
import net.starlegacy.util.stripColor
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class Multiblock {
	abstract val name: String

	abstract val signText: List<String>

	open fun matchesSign(lines: Array<String>): Boolean {
		for (i in 0..3) {
			if (signText[i].isNotEmpty() && signText[i] != lines[i]) {
				return false
			}
		}
		return true
	}

	protected abstract fun MultiblockShape.buildStructure()

	val shape by lazy { MultiblockShape().apply { buildStructure() } }

	fun signMatchesStructure(sign: Sign, loadChunks: Boolean = true, particles: Boolean = false): Boolean {
		val inward = sign.getFacing().oppositeFace
		return signMatchesStructure(sign.location, inward, loadChunks, particles)
	}

	/** Checks only the blocks without checking if the sign location is actually a sign */
	fun signMatchesStructure(
		signLocation: Location,
		inward: BlockFace,
		loadChunks: Boolean = true,
		particles: Boolean = false
	): Boolean {
		val x = signLocation.blockX + inward.modX
		val y = signLocation.blockY + inward.modY
		val z = signLocation.blockZ + inward.modZ

		if (!isValidYLevel(y)) return false

		val originBlock: Block = if (loadChunks) signLocation.world.getBlockAt(x, y, z)
		else getBlockIfLoaded(signLocation.world, x, y, z) ?: return false

		return blockMatchesStructure(originBlock, inward, loadChunks, particles)
	}

	fun blockMatchesStructure(
		originBlock: Block,
		inward: BlockFace,
		loadChunks: Boolean = true,
		particles: Boolean = false
	): Boolean {
		return shape.checkRequirements(originBlock, inward, loadChunks, particles)
	}

	open fun matchesUndetectedSign(sign: Sign): Boolean {
		return sign.getLine(0).equals("[$name]", ignoreCase = true)
	}

	open fun setupSign(player: Player, sign: Sign) {
		onTransformSign(player, sign)

		for (i in 0..3) {
			val text = signText[i]
			if (text.stripColor().trim().isNotEmpty()) {
				sign.setLine(i, text)
			}
		}

		sign.update()
	}

	protected fun createSignText(line1: String?, line2: String?, line3: String?, line4: String?): List<String> =
		sequenceOf(line1 ?: "", line2 ?: "", line3 ?: "", line4 ?: "").map { it.colorize() }.toList()

	protected open fun onTransformSign(player: Player, sign: Sign) {}
}
