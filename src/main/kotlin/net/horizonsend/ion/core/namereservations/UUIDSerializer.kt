package net.horizonsend.ion.core.namereservations

import java.util.UUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class UUIDSerializer: KSerializer<UUID> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", STRING)

	override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())

	override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}