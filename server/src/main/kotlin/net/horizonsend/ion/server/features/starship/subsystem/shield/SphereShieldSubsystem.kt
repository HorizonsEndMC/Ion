package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.multiblock.type.particleshield.SphereShieldMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import org.bukkit.World
import org.bukkit.block.Sign

class SphereShieldSubsystem(
	starship: ActiveStarship,
	sign: Sign,
	multiblock: SphereShieldMultiblock
) : ShieldSubsystem(starship, sign, multiblock) {
	val maxRange = multiblock.maxRange
	private val maxRangeSquared = maxRange.squared()

	override var power: Int = maxPower
		set(value) {
			field = value.coerceIn(0, maxPower)
		}

	override fun containsPosition(world: World, blockPos: Vec3i): Boolean {
		if (starship.world.uid != world.uid) {
			return false
		}

		val x1 = pos.x.toDouble()
		val y1 = pos.y.toDouble()
		val z1 = pos.z.toDouble()

		val x2 = blockPos.x.toDouble()
		val y2 = blockPos.y.toDouble()
		val z2 = blockPos.z.toDouble()

		val distanceSquared = distanceSquared(x1, y1, z1, x2, y2, z2)

		return distanceSquared <= maxRangeSquared
	}
}
