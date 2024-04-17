package net.horizonsend.ion.common.utils.configuration

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

object ComponentKSerializer : KSerializer<Component> {
	private val gsonSerializer = GsonComponentSerializer.gson()
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName = "Component", kind = PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): Component {
		return gsonSerializer.deserialize(decoder.decodeString())
	}

	override fun serialize(encoder: Encoder, value: Component) {
		encoder.encodeString(gsonSerializer.serialize(value))
	}
}
