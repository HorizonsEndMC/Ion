package net.horizonsend.ion.server.configuration.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.state.BlockState

class BlockStateSerializer : KSerializer<BlockState> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BlockState", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): BlockState {
		val state = decoder.decodeString()

		return NbtUtils.readBlockState(BuiltInRegistries.BLOCK, NbtUtils.snbtToStructure(state))
	}

	override fun serialize(encoder: Encoder, value: BlockState) {
		encoder.encodeString(NbtUtils.structureToSnbt(NbtUtils.writeBlockState(value)))
	}
}
