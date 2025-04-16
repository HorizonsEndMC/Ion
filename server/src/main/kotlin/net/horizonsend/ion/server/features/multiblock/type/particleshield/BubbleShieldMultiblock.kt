package net.horizonsend.ion.server.features.multiblock.type.particleshield

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.msg
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.pow

object BoxShieldMultiblock : ShieldMultiblock(), DisplayNameMultilblock {
	private const val MIN_DIMENSION = 9
	private const val MAX_DIMENSION = 27
	private const val MIN_VOLUME = 256
	private const val MAX_VOLUME = 8192

	override val displayName: Component get() = text("Bubble Shield")
	override val description: Component get() = text("Protects a starship from explosion damage within a user-specified ellipsoid region, between $MIN_DIMENSION and $MAX_DIMENSION blocks long for each axis.")

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

		val volume = 4/3 * pi * width * height * length / 8
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
				x(-1).anyStairs()
				x(+1).anyStairs()
			}
      
			y(+0) {
				x(-1).anyWallBlock()
				x(+0).anyGlass()
				x(+1).anyWallBlock()
			}

      y(+1) {
				x(-1).anyStairs()
				x(+1).anyStairs()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).ironBlock()
				x(+1).ironBlock()
			}
      
			y(+0) {
				x(-1).sponge()
				x(+0).steelBlock()
				x(+1).sponge()
			}

      y(+1) {
				x(-1).ironBlock()
				x(+1).ironBlock()
			}
    }

    z(+2) {
      y(-1) {
				x(-1).anyStairs()
				x(+1).anyStairs()
			}
      
			y(+0) {
				x(-1).anyWallBlock()
				x(+0).anyGlass()
				x(+1).anyWallBlock()
			}

      y(+1) {
				x(-1).anyStairs()
				x(+1).anyStairs()
			}
		}
	}

	override fun getShieldBlocks(sign: Sign): List<Vec3i> {
		val dimensions = sign.getLine(3)
			.replace(",", " ")
			.split(" ")
			.map { it.toInt() }
		val width = dimensions[0]
		val height = dimensions[1]
		val length = dimensions[2]

		val inward = sign.getFacing().oppositeFace
		val right = inward.rightFace

		val dw = width / 2
		val dl = length / 2

		val dx = abs(dw * right.modX + dl * inward.modX)
		val dy = height / 2
		val dz = abs(dw * right.modZ + dl * inward.modZ)

		val blocks = mutableListOf<Vec3i>()

		for (x in (-dx)..(dx)) {
			for (y in (-dy)..(dy)) {
				for (z in (-dz)..(dz)) {
          val ellipsoidExpression = (x/dx).pow(2) + (y/dy).pow(2) + (z/dz).pow(2)
          if( abs(ellipsoidExpression - 1) <= 0.25) {
            continue
          }

					blocks.add(Vec3i(x, y, z))
				}
			}
		}

		return blocks
	}
}
