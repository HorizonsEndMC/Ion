package net.horizonsend.ion.server.features.starship.type.restriction

import net.horizonsend.ion.server.features.starship.FLYABLE_BLOCKS
import org.bukkit.Material
import java.util.EnumSet

class DetectionParameters(
	val minSize: Int = 25,
	val maxSize: Int = 100,
	val containerPercent: Double,
	val concretePercent: Double = 0.3,
	val crateLimitMultiplier: Double,
	val pilotableBlockList: EnumSet<Material> = FLYABLE_BLOCKS
) {
}
