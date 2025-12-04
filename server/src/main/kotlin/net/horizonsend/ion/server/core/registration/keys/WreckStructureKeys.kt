package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WRECK_STRUCTURE
import net.horizonsend.ion.server.features.world.generation.feature.meta.wreck.WreckStructure

object WreckStructureKeys : KeyRegistry<WreckStructure>(WRECK_STRUCTURE, WreckStructure::class) {
	val TUTORIAL_ESCAPE_POD = registerKey("TUTORIAL_ESCAPE_POD")
}
