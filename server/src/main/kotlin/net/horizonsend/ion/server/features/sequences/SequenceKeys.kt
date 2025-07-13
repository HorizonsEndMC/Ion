package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys

object SequenceKeys : KeyRegistry<Sequence>(RegistryKeys.SEQUENCE, Sequence::class) {
	val TUTORIAL = registerKey("TUTORIAL")
}
