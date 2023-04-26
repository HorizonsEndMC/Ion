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
import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataContainer
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
				is ByteTag -> primitive.raw.set(name, tag)
				is ShortTag -> primitive.raw.set(name, tag)
				is IntTag -> primitive.raw.set(name, tag)
				is LongTag -> primitive.raw.set(name, tag)
				is FloatTag -> primitive.raw.set(name, tag)
				is DoubleTag -> primitive.raw.set(name, tag)
				is ByteArrayTag -> primitive.raw.set(name, tag)
				is StringTag -> primitive.raw.set(name, tag)
				is CompoundTag -> primitive.raw.set(name, tag)
				is IntArrayTag -> primitive.raw.set(name, tag)
				is LongArrayTag -> primitive.raw.set(name, tag)
				is ListTag -> primitive.raw.set(name, tag)
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
