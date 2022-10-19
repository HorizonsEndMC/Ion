package net.starlegacy.feature.starship.subsystem.shield

import net.starlegacy.feature.multiblock.particleshield.SphereShieldMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.squared
import org.bukkit.block.Block
import org.bukkit.block.Sign

class SphereShieldSubsystem(
	starship: ActiveStarship,
	sign: Sign,
	multiblock: SphereShieldMultiblock
) : ShieldSubsystem(starship, sign, multiblock) {
	val maxRange = multiblock.maxRange
	private val maxRangeSquared = maxRange.squared()

	override fun containsBlock(block: Block): Boolean {
		if (starship.world.uid != block.world.uid) {
			return false
		}

		val x1 = pos.x.toDouble()
		val y1 = pos.y.toDouble()
		val z1 = pos.z.toDouble()

		val x2 = block.x.toDouble()
		val y2 = block.y.toDouble()
		val z2 = block.z.toDouble()

		val distanceSquared = distanceSquared(x1, y1, z1, x2, y2, z2)

		return distanceSquared <= maxRangeSquared
	}
}
