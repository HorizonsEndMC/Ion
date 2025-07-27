package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.server.features.multiblock.type.particleshield.BubbleShieldMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import kotlin.math.abs
import kotlin.math.pow

class BubbleShieldSubsystem(
	starship: ActiveStarship,
	sign: Sign,
	multiblock: BubbleShieldMultiblock
) : ShieldSubsystem(starship, sign, multiblock) {
	override var power: Int = maxPower
		set(value) {
			field = value.coerceIn(0, maxPower)
		}

	private val width: Int
	private val height: Int
	private val length: Int

  private data class Ellipsoid(val maxX: Double, val maxY: Double, val maxZ: Double)

  private val cachedShapes = mutableMapOf<BlockFace, Ellipsoid>()

	init {
		val dimensions = sign.getLine(3)
			.replace(",", " ")
			.split(" ")
			.map { it.toInt() }
		width = dimensions[0]
		height = dimensions[1]
		length = dimensions[2]

		cacheShape { face -> face }
		cacheShape { face -> face.rightFace }
		cacheShape { face -> face.leftFace }
		cacheShape { face -> face.oppositeFace }
	}

	override fun containsPosition(world: World, blockPos: Vec3i): Boolean {
		if (starship.world.uid != world.uid) {
			return false
		}

    val shape = getShape()
    val semiAxisX = shape.maxX
    val semiAxisY = shape.maxY  //semi-axis lengths
    val semiAxisZ = shape.maxZ

		val lx = blockPos.x - pos.x
		val ly = blockPos.y - pos.y  //impact location relative to center
		val lz = blockPos.z - pos.z

    return 1 >= (lx/semiAxisX).pow(2) + (ly/semiAxisY).pow(2) + (lz/semiAxisZ).pow(2)
	}

  private fun cacheShape(getRotatedFace: (BlockFace) -> BlockFace) {
		val key = getRotatedFace(this.face)
		val right = key.rightFace

		val dx = (width / 2.0) * abs(right.modX) + (length / 2.0) * abs(key.modX)
		val dy = height / 2.0
		val dz = (width / 2.0) * abs(right.modZ) + (length / 2.0) * abs(key.modZ)

		cachedShapes[key] = Ellipsoid(dx, dy, dz)
	}

	private fun getShape(): Ellipsoid {
		return cachedShapes.getValue(face)
	}
}
