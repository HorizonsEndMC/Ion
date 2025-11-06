package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceContext
import org.bukkit.event.Event

class TriggerContext(
	val sequence: IonRegistryKey<Sequence, out Sequence>,
	val callingTrigger: SequenceTriggerType<*>,
	val sequenceContext: SequenceContext,
	val event: Event?
)
