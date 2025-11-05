package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase

class Sequence(val key: IonRegistryKey<Sequence, out Sequence>, val firstPhase: IonRegistryKey<SequencePhase, out SequencePhase>) {
}
