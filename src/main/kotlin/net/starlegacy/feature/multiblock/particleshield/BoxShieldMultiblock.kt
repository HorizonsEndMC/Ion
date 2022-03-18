package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.util.msg
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object BoxShieldMultiblock : ShieldMultiblock() {
	private const val MIN_DIMENSION = 5
	private const val MAX_DIMENSION = 21
	private const val MIN_VOLUME = 256
	private const val MAX_VOLUME = 8192

	override fun setupSign(player: Player, sign: Sign) {
		val line = sign.getLine(2)

		val split = line.replace(",", " ").split(" ")

		if (split.size != 3 || split.any { it.toIntOrNull() == null }) {
			player msg "&cInvalid dimensions: $line"
			return
		}

		val integers = split.map { it.toInt() }

		if (integers.any { it !in MIN_DIMENSION..MAX_DIMENSION }) {
			player msg "&cAll dimensions must be at least $MIN_DIMENSION and at most $MAX_DIMENSION"
			return
		}

		if (integers.any { it % 2 == 0 }) {
			player msg "&cAll dimensions must be odd numbers"
			return
		}

		val width = integers[0]
		val height = integers[1]
		val length = integers[2]

		val volume = width * height * length
		if (volume !in MIN_VOLUME..MAX_VOLUME) {
			player msg "&cVolume is $volume but must be at least $MIN_VOLUME and at most $MAX_VOLUME"
			return
		}

		super.setupSign(player, sign)
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		sign.setLine(3, sign.getLine(2))
		super.onTransformSign(player, sign)
	}

	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Projector",
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).anyGlassPane()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+0).anyGlassPane()
				x(+1).ironBlock()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).titaniumBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
			}
		}
	}
}
