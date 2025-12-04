package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WRECK_STRUCTURE
import net.horizonsend.ion.server.core.registration.keys.WreckStructureKeys
import net.horizonsend.ion.server.features.world.generation.feature.meta.wreck.WreckStructure

class WreckStructureRegistry : Registry<WreckStructure>(WRECK_STRUCTURE) {
	override fun getKeySet(): KeyRegistry<WreckStructure> = WreckStructureKeys

	override fun boostrap() {
		register(WreckStructureKeys.TUTORIAL_ESCAPE_POD, WreckStructure(WreckStructureKeys.TUTORIAL_ESCAPE_POD, "TutorialEscapePod"))
	}
}
