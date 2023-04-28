package net.horizonsend.ion.server.features.space.data

import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.BLOCK_ENTITY
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.BLOCK_STATE
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class BlockData(val blockState: BlockState, var blockEntityTag: CompoundTag?) {
	companion object : PersistentDataType<PersistentDataContainer, BlockData> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = BlockData::class.java

		override fun toPrimitive(complex: BlockData, context: PersistentDataAdapterContext): PersistentDataContainer {
			val primitive = context.newPersistentDataContainer()
			primitive.set(BLOCK_STATE, CompoundTagType, NbtUtils.writeBlockState(complex.blockState))
			if (complex.blockEntityTag != null) primitive.set(BLOCK_ENTITY, CompoundTagType, complex.blockEntityTag!!)
			return primitive
		}

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): BlockData {
			val lookup = BuiltInRegistries.BLOCK.asLookup()
			val compoundTag = primitive.get(BLOCK_STATE, CompoundTagType)!!
			val blockState = NbtUtils.readBlockState(lookup, compoundTag)
			val blockEntity = primitive.get(BLOCK_ENTITY, CompoundTagType)
			return BlockData(blockState, blockEntity)
		}

		val AIR = BlockData(Blocks.AIR.defaultBlockState(), null)
	}

	override fun hashCode(): Int {
		var result = blockState.hashCode()
		result = 31 * result + (blockEntityTag?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BlockData

		if (blockState != other.blockState) return false
		if (blockEntityTag != other.blockEntityTag) return false

		return true
	}
}
