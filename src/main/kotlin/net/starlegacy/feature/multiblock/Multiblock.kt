package net.starlegacy.feature.multiblock

import net.starlegacy.feature.progression.advancement.Advancements
import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.util.colorize
import net.starlegacy.util.getBlockIfLoaded
import net.starlegacy.util.getFacing
import net.starlegacy.util.gray
import net.starlegacy.util.isValidYLevel
import net.starlegacy.util.msg
import net.starlegacy.util.stripColor
import net.starlegacy.util.yellow
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class Multiblock {
	abstract val name: String

	abstract val signText: List<String>

	abstract val advancement: SLAdvancement?

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
		if (!isUnlocked(player)) {
			if (player.hasPermission("dutymode")) {
				player msg yellow("Bypassed advancement $advancement for multiblock ${javaClass.simpleName}")
			} else {
				player msg gray("You don't have access to this multiblock! To detect it, you need the advancement $advancement.")
				return
			}
		}

		onTransformSign(player, sign)

		for (i in 0..3) {
			val text = signText[i]
			if (text.stripColor().trim().isNotEmpty()) {
				sign.setLine(i, text)
			}
		}

		sign.update()
	}

	fun isUnlocked(player: Player): Boolean {
		// if the advancement is null, return true
		// if the advancement is not null and they have it, return true
		// if the advancement is not null and they don't have it, return false
		return advancement?.let { Advancements.has(player, it) } != false
	}

	protected fun createSignText(line1: String?, line2: String?, line3: String?, line4: String?): List<String> =
		sequenceOf(line1 ?: "", line2 ?: "", line3 ?: "", line4 ?: "").map { it.colorize() }.toList()

	protected open fun onTransformSign(player: Player, sign: Sign) {}
}
