package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i

abstract class Sequence(val key: IonRegistryKey<Sequence, out Sequence>, val firstPhase: IonRegistryKey<SequencePhase, out SequencePhase>) {
	abstract fun getOrigin(): Vec3i

	fun getAdjustedCoordinate(relativeX: Int, relativeY: Int, relativeZ: Int): Vec3i = getOrigin().plus(Vec3i(relativeX, relativeY, relativeZ))

	fun getContext(): SequenceContext = SequenceContext(key)
}
