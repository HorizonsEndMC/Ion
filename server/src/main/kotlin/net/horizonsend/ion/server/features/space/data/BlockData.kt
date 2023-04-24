package net.horizonsend.ion.server.features.space.data

import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.BLOCK_ENTITY
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.BLOCK_STATE
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.STORED_CHUNK_BLOCKS
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Chunk
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class BlockData(val blockState: BlockState, val blockEntityTag: CompoundTag?) {
	companion object : PersistentDataType<PersistentDataContainer, BlockData> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = BlockData::class.java

		override fun toPrimitive(complex: BlockData, context: PersistentDataAdapterContext): PersistentDataContainer {
			val primitive = context.newPersistentDataContainer()
			primitive.set(BLOCK_STATE, CompoundTagType, NbtUtils.writeBlockState(complex.blockState))
			if (complex.blockEntityTag != null) primitive.set(BLOCK_ENTITY, CompoundTagType, complex.blockEntityTag)
			return primitive
		}

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): BlockData {
			val lookup = BuiltInRegistries.BLOCK.asLookup()
			val compoundTag = primitive.get(BLOCK_STATE, CompoundTagType)!!
			val blockState = NbtUtils.readBlockState(lookup, compoundTag)
			val blockEntity = primitive.get(BLOCK_ENTITY, CompoundTagType)
			return BlockData(blockState, blockEntity)
		}
	}
}
