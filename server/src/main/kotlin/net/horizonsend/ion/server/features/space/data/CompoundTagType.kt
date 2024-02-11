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
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataContainer
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object CompoundTagType : PersistentDataType<PersistentDataContainer, CompoundTag> {
	override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
	override fun getComplexType(): Class<CompoundTag> = CompoundTag::class.java

	override fun toPrimitive(complex: CompoundTag, context: PersistentDataAdapterContext): PersistentDataContainer {
		val primitive = context.newPersistentDataContainer() as CraftPersistentDataContainer

		for ((name, tag) in complex.tags) {
			primitive.raw

			when (tag) {
				is ByteTag -> primitive.raw[name] = tag
				is ShortTag -> primitive.raw[name] = tag
				is IntTag -> primitive.raw[name] = tag
				is LongTag -> primitive.raw[name] = tag
				is FloatTag -> primitive.raw[name] = tag
				is DoubleTag -> primitive.raw[name] = tag
				is ByteArrayTag -> primitive.raw[name] =tag
				is StringTag -> primitive.raw[name] =tag
				is CompoundTag -> primitive.raw[name] =tag
				is IntArrayTag -> primitive.raw[name] =tag
				is LongArrayTag -> primitive.raw[name] =tag
				is ListTag -> primitive.raw[name] =tag
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
