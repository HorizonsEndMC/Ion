package net.horizonsend.ion.server.configuration.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem

object SubsystemSerializer : KSerializer<Class<StarshipSubsystem>> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Multiblock", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): Class<StarshipSubsystem> {
		val name = decoder.decodeString()

		@Suppress("UNCHECKED_CAST")
		return Class.forName(name) as? Class<StarshipSubsystem> ?: throw SerializationException("")
	}

	override fun serialize(encoder: Encoder, value: Class<StarshipSubsystem>) {
		encoder.encodeString(value.name)
	}
}
