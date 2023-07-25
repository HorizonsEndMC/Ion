package net.horizonsend.ion.server.features.multiblock

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isValidYLevel
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class Multiblock {
	abstract val name: String

	abstract val signText: Array<Component?>

	open fun matchesSign(lines: Array<Component>): Boolean {
		for (i in 0..3) {
			if (signText[i] != null && signText[i] != lines[i]) {
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

		val originBlock: Block = if (loadChunks) {
			signLocation.world.getBlockAt(x, y, z)
		} else {
			getBlockIfLoaded(signLocation.world, x, y, z) ?: return false
		}

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
		return (sign.line(0) as TextComponent).content().equals("[$name]", ignoreCase = true)
	}

	open fun setupSign(player: Player, sign: Sign) {
		onTransformSign(player, sign)

		for (i in 0..3) {
			val text = signText[i]
			if (text != null) {
				sign.line(i, text)
			}
		}

		sign.update()
	}

	protected fun createSignText(line1: String?, line2: String?, line3: String?, line4: String?): Array<Component?> {
		val serializer = LegacyComponentSerializer.legacyAmpersand()

		return arrayOf(
			if (line1 != null) serializer.deserialize(line1) else null,
			if (line2 != null) serializer.deserialize(line2) else null,
			if (line3 != null) serializer.deserialize(line3) else null,
			if (line4 != null) serializer.deserialize(line4) else null
		)
	}

	protected open fun onTransformSign(player: Player, sign: Sign) {}
}
