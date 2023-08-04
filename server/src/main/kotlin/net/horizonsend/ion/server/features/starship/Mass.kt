package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.server.miscellaneous.utils.MATERIALS
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import org.bukkit.Material
import java.util.EnumMap
import kotlin.math.max

object Mass {
	val BLAST_RESIST_MASS_MULTIPLIER = 5.0

	private val massMap = EnumMap(
		MATERIALS.filter { it.isBlock }.associateWith {
			when {
				it.isShulkerBox -> 1000.0
				it.isConcrete -> 1.0
				else -> max(it.blastResistance * BLAST_RESIST_MASS_MULTIPLIER, 1.0)
			}
		}
	)

	operator fun set(material: Material, mass: Double) {
		massMap[material] = mass
	}

	operator fun get(material: Material): Double = massMap[material] ?: error("$material isn't in the mass map")
}
