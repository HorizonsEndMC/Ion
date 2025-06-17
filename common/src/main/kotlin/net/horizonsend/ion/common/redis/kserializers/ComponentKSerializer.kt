package net.horizonsend.ion.common.redis.kserializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.horizonsend.ion.common.utils.text.gson
import net.kyori.adventure.text.Component

object ComponentKSerializer : KSerializer<Component> {
	override fun deserialize(decoder: Decoder): Component {
		return gson.deserialize(decoder.decodeString())
	}

	override fun serialize(encoder: Encoder, value: Component) {
		encoder.encodeString(gson.serialize(value))
	}

	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Component", PrimitiveKind.STRING)
}
