package net.starlegacy.feature.starship

import net.starlegacy.util.MATERIALS
import net.starlegacy.util.isConcrete
import net.starlegacy.util.isShulkerBox
import org.bukkit.Material
import java.util.EnumMap

object Mass {
	val BLAST_RESIST_MASS_MULTIPLIER = 5.0

	private val massMap = EnumMap(
		MATERIALS.filter { it.isBlock }.associateWith {
			when {
				it.isShulkerBox -> 1000.0
				it.isConcrete -> 1.0
				else -> it.blastResistance * BLAST_RESIST_MASS_MULTIPLIER
			}
		}
	)

	operator fun set(material: Material, mass: Double) {
		massMap[material] = mass
	}

	operator fun get(material: Material): Double = massMap[material] ?: error("$material isn't in the mass map")
}
