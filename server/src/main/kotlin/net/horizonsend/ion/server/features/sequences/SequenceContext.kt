package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i

/**
 * Contains data specific to sequence types, such as origin
 **/
class SequenceContext(val sequence: IonRegistryKey<Sequence, out Sequence>) {
	fun getOrigin(): Vec3i = sequence.getValue().getOrigin()
	fun getAdjustedCoordinate(relative: Vec3i): Vec3i = sequence.getValue().getOrigin().plus(relative)
	fun getAdjustedCoordinate(relativeX: Int, relativeY: Int, relativeZ: Int): Vec3i = sequence.getValue().getOrigin().plus(Vec3i(relativeX, relativeY, relativeZ))
}