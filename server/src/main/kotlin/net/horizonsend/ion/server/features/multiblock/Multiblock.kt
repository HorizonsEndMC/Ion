package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.isValidYLevel
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player

abstract class Multiblock {
	abstract val name: String
	open val alternativeDetectionNames: Array<String> = arrayOf()

	abstract val signText: Array<Component?>

	open val requiredPermission: String? = null

	open fun matchesSign(lines: List<Component>): Boolean {
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
			val block = signLocation.world.getBlockAt(x, y, z)
			block
		} else {
			getBlockIfLoaded(signLocation.world, x, y, z) ?: return false
		}

		return blockMatchesStructure(originBlock, inward, loadChunks, particles)
	}

	fun blockMatchesStructure(
		originBlock: Block,
		inward: BlockFace,
		loadChunks: Boolean = true,
		particles: Boolean = false,
	): Boolean {
		return shape.checkRequirements(originBlock, inward, loadChunks, particles)
	}

	open fun matchesUndetectedSign(sign: Sign): Boolean {
		val line = sign.getSide(Side.FRONT).line(0)
		val content = (line as? TextComponent)?.content() ?: line.plainText()

		return alternativeDetectionNames
			.toMutableList()
			.plus(name)
			.map { "[$it]" }
			.any { it.equals(content, ignoreCase = true) }
	}

	open fun setupSign(player: Player, sign: Sign) {
		onTransformSign(player, sign)

		for (i in 0..3) {
			val text = signText[i]
			if (text != null) {
				sign.front().line(i, text)
			}
		}

		sign.isWaxed = true
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

	protected fun createSignText(line1: Component?, line2: Component?, line3: Component?, line4: Component?): Array<Component?> {
		return arrayOf(line1, line2, line3, line4)
	}

	protected open fun onTransformSign(player: Player, sign: Sign) {}
}
