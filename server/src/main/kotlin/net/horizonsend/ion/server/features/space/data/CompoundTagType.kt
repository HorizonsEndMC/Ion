package net.horizonsend.ion.server.features.space.data

import net.minecraft.nbt.ByteArrayTag
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntArrayTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongArrayTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.ShortTag
import net.minecraft.nbt.StringTag
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object CompoundTagType : PersistentDataType<PersistentDataContainer, CompoundTag> {
	override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
	override fun getComplexType(): Class<CompoundTag> = CompoundTag::class.java

	override fun toPrimitive(complex: CompoundTag, context: PersistentDataAdapterContext): PersistentDataContainer {
		val primitive = context.newPersistentDataContainer() as CraftPersistentDataContainer

		for (key in complex.allKeys) {
			val tag = complex.get(key)
			primitive.raw

			when (tag) {
				is ByteTag -> primitive.raw[key] = tag
				is ShortTag -> primitive.raw[key] = tag
				is IntTag -> primitive.raw[key] = tag
				is LongTag -> primitive.raw[key] = tag
				is FloatTag -> primitive.raw[key] = tag
				is DoubleTag -> primitive.raw[key] = tag
				is ByteArrayTag -> primitive.raw[key] =tag
				is StringTag -> primitive.raw[key] =tag
				is CompoundTag -> primitive.raw[key] =tag
				is IntArrayTag -> primitive.raw[key] =tag
				is LongArrayTag -> primitive.raw[key] =tag
				is ListTag -> primitive.raw[key] =tag
				else -> {
					throw NotImplementedError("Impossible to get data from generic Tag.")
				}
			}
		}

		return primitive
	}

	override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): CompoundTag {
		return (primitive as CraftPersistentDataContainer).toTagCompound()
	}
}
