package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys

class SequenceRegistry : Registry<Sequence>(RegistryKeys.SEQUENCE) {
	override fun getKeySet(): KeyRegistry<Sequence> = SequenceKeys

	override fun boostrap() {
		register(SequenceKeys.TUTORIAL, Sequence(SequenceKeys.TUTORIAL, SequencePhaseKeys.TUTORIAL_START))
	}
}
