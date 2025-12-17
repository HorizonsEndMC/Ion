package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WRECK_STRUCTURE
import net.horizonsend.ion.server.core.registration.keys.WreckStructureKeys
import net.horizonsend.ion.server.features.world.generation.feature.meta.wreck.EmptyWreckStructure
import net.horizonsend.ion.server.features.world.generation.feature.meta.wreck.SchematicWreckStructure
import net.horizonsend.ion.server.features.world.generation.feature.meta.wreck.WreckStructure

class WreckStructureRegistry : Registry<WreckStructure>(WRECK_STRUCTURE) {
	override fun getKeySet(): KeyRegistry<WreckStructure> = WreckStructureKeys

	override fun boostrap() {
		register(WreckStructureKeys.EMPTY, EmptyWreckStructure)
		register(WreckStructureKeys.CORVETTE_HIGH, SchematicWreckStructure(WreckStructureKeys.CORVETTE_HIGH, "CorvetteHighN"))
		register(WreckStructureKeys.CORVETTE_LOW_01, SchematicWreckStructure(WreckStructureKeys.CORVETTE_LOW_01, "CorvetteLow01N"))
		register(WreckStructureKeys.CORVETTE_LOW_02, SchematicWreckStructure(WreckStructureKeys.CORVETTE_LOW_02, "CorvetteLow02N"))
		register(WreckStructureKeys.CORVETTE_MID_01, SchematicWreckStructure(WreckStructureKeys.CORVETTE_MID_01, "CorvetteMid01N"))
		register(WreckStructureKeys.DESTROYER_HIGH, SchematicWreckStructure(WreckStructureKeys.DESTROYER_HIGH, "DestroyerHighN"))
		register(WreckStructureKeys.DESTROYER_LOW_01, SchematicWreckStructure(WreckStructureKeys.DESTROYER_LOW_01, "DestroyerLow01N"))
		register(WreckStructureKeys.DESTROYER_LOW_02, SchematicWreckStructure(WreckStructureKeys.DESTROYER_LOW_02, "DestroyerLow02N"))
		register(WreckStructureKeys.DESTROYER_MID_01, SchematicWreckStructure(WreckStructureKeys.DESTROYER_MID_01, "DestroyerMid01N"))
		register(WreckStructureKeys.DESTROYER_MID_02, SchematicWreckStructure(WreckStructureKeys.DESTROYER_MID_02, "DestroyerMid02N"))
		register(WreckStructureKeys.FRIGATE_HIGH, SchematicWreckStructure(WreckStructureKeys.FRIGATE_HIGH, "FrigateHighN"))
		register(WreckStructureKeys.FRIGATE_LOW_01, SchematicWreckStructure(WreckStructureKeys.FRIGATE_LOW_01, "FrigateLow01N"))
		register(WreckStructureKeys.FRIGATE_LOW_02, SchematicWreckStructure(WreckStructureKeys.FRIGATE_LOW_02, "FrigateLow02N"))
		register(WreckStructureKeys.FRIGATE_MID_01, SchematicWreckStructure(WreckStructureKeys.FRIGATE_MID_01, "FrigateMid01N"))
		register(WreckStructureKeys.FRIGATE_MID_02, SchematicWreckStructure(WreckStructureKeys.FRIGATE_MID_02, "FrigateMid02N"))
		register(WreckStructureKeys.GUNSHIP_HIGH, SchematicWreckStructure(WreckStructureKeys.GUNSHIP_HIGH, "GunshipHighN"))
		register(WreckStructureKeys.GUNSHIP_LOW_01, SchematicWreckStructure(WreckStructureKeys.GUNSHIP_LOW_01, "GunshipLow01N"))
		register(WreckStructureKeys.GUNSHIP_LOW_02, SchematicWreckStructure(WreckStructureKeys.GUNSHIP_LOW_02, "GunshipLow02N"))
		register(WreckStructureKeys.GUNSHIP_MID_01, SchematicWreckStructure(WreckStructureKeys.GUNSHIP_MID_01, "GunshipMid01N"))
		register(WreckStructureKeys.GUNSHIP_MID_02, SchematicWreckStructure(WreckStructureKeys.GUNSHIP_MID_02, "GunshipMid02N"))
		register(WreckStructureKeys.STARFIGHTER_HIGH, SchematicWreckStructure(WreckStructureKeys.STARFIGHTER_HIGH, "StarfighterHighN"))
		register(WreckStructureKeys.STARFIGHTER_LOW_01, SchematicWreckStructure(WreckStructureKeys.STARFIGHTER_LOW_01, "StarfighterLow01N"))
		register(WreckStructureKeys.STARFIGHTER_LOW_02, SchematicWreckStructure(WreckStructureKeys.STARFIGHTER_LOW_02, "StarfighterLow02N"))
		register(WreckStructureKeys.STARFIGHTER_MID_01, SchematicWreckStructure(WreckStructureKeys.STARFIGHTER_MID_01, "StarfighterMid01N"))
		register(WreckStructureKeys.STARFIGHTER_MID_02, SchematicWreckStructure(WreckStructureKeys.STARFIGHTER_MID_02, "StarfighterMid02N"))
	}
}
