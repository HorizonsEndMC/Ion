package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.server.features.multiblock.type.particleshield.BoxShieldMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import kotlin.collections.set
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

  private data class Ellipsoid(val max: Vec3i)

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
    val semiAxisX = shape.max.x
    val semiAxisY = shape.max.y  //semi-axis length 
    val semiAxisZ = shape.max.z
		
		val lx = blockPos.x - pos.x
		val ly = blockPos.y - pos.y  //impact location relative to center
		val lz = blockPos.z - pos.z

    return 1 >= (lx/semiAxisX).pow(2) + (ly/semiAxisY).pow(2) + (lz/semiAxisZ).pow(2)
	}

  private fun cacheShape(getRotatedFace: (BlockFace) -> BlockFace) {
		val key = getRotatedFace(this.face)
		val right = key.rightFace

		val dx = (width / 2) * abs(right.modX) + (length / 2) * abs(key.modX)
		val dy = height / 2
		val dz = (width / 2) * abs(right.modZ) + (length / 2) * abs(key.modZ)

		cachedShapes[key] = Ellipsoid(Vec3i(dx, dy, dz))
	}

	private fun getShape(): Ellipsoid {
		return cachedShapes.getValue(face)
	}
}
