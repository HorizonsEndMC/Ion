package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WRECK_STRUCTURE
import net.horizonsend.ion.server.features.world.generation.feature.meta.wreck.WreckStructure

object WreckStructureKeys : KeyRegistry<WreckStructure>(WRECK_STRUCTURE, WreckStructure::class) {
	val EMPTY = registerKey("EMPTY")
	val CORVETTE_HIGH = registerKey("CORVETTE_HIGH")
	val CORVETTE_LOW_01 = registerKey("CORVETTE_LOW_01")
	val CORVETTE_LOW_02 = registerKey("CORVETTE_LOW_02")
	val CORVETTE_MID_01 = registerKey("CORVETTE_MID_01")
	val DESTROYER_HIGH = registerKey("DESTROYER_HIGH")
	val DESTROYER_LOW_01 = registerKey("DESTROYER_LOW_01")
	val DESTROYER_LOW_02 = registerKey("DESTROYER_LOW_02")
	val DESTROYER_MID_01 = registerKey("DESTROYER_MID_01")
	val DESTROYER_MID_02 = registerKey("DESTROYER_MID_02")
	val FRIGATE_HIGH = registerKey("FRIGATE_HIGH")
	val FRIGATE_LOW_01 = registerKey("FRIGATE_LOW_01")
	val FRIGATE_LOW_02 = registerKey("FRIGATE_LOW_02")
	val FRIGATE_MID_01 = registerKey("FRIGATE_MID_01")
	val FRIGATE_MID_02 = registerKey("FRIGATE_MID_02")
	val GUNSHIP_HIGH = registerKey("GUNSHIP_HIGH")
	val GUNSHIP_LOW_01 = registerKey("GUNSHIP_LOW_01")
	val GUNSHIP_LOW_02 = registerKey("GUNSHIP_LOW_02")
	val GUNSHIP_MID_01 = registerKey("GUNSHIP_MID_01")
	val GUNSHIP_MID_02 = registerKey("GUNSHIP_MID_02")
	val STARFIGHTER_HIGH = registerKey("STARFIGHTER_HIGH")
	val STARFIGHTER_LOW_01 = registerKey("STARFIGHTER_LOW_01")
	val STARFIGHTER_LOW_02 = registerKey("STARFIGHTER_LOW_02")
	val STARFIGHTER_MID_01 = registerKey("STARFIGHTER_MID_01")
	val STARFIGHTER_MID_02 = registerKey("STARFIGHTER_MID_02")
}
