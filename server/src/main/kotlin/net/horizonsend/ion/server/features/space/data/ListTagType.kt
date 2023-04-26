package net.horizonsend.ion.server.features.space.data

import net.horizonsend.ion.server.IonServer
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
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataContainer
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ListTagType : PersistentDataType<PersistentDataContainer, ListTag> {
	override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
	override fun getComplexType(): Class<ListTag> = ListTag::class.java

	override fun toPrimitive(complex: ListTag, context: PersistentDataAdapterContext): PersistentDataContainer {
		val primitive = context.newPersistentDataContainer()

		var index = 1

		for (entry in complex) {
			val key = NamespacedKey(IonServer, index.toString())

			index++

			when (entry) {
				is ByteTag -> primitive.set(key, PersistentDataType.BYTE, entry.asByte)
				is ShortTag -> primitive.set(key, PersistentDataType.SHORT, entry.asShort)
				is IntTag -> primitive.set(key, PersistentDataType.INTEGER, entry.asInt)
				is LongTag -> primitive.set(key, PersistentDataType.LONG, entry.asLong)
				is FloatTag -> primitive.set(key, PersistentDataType.FLOAT, entry.asFloat)
				is DoubleTag -> primitive.set(key, PersistentDataType.DOUBLE, entry.asDouble)
				is ByteArrayTag -> primitive.set(key, PersistentDataType.BYTE_ARRAY, entry.asByteArray)
				is StringTag -> primitive.set(key, PersistentDataType.STRING, entry.asString)
				is CompoundTag -> primitive.set(key, CompoundTagType, entry)
				is IntArrayTag -> primitive.set(key, PersistentDataType.INTEGER_ARRAY, entry.asIntArray)
				is LongArrayTag -> primitive.set(key, PersistentDataType.LONG_ARRAY, entry.asLongArray)
				is ListTag -> primitive.set(key, ListTagType, entry)
				else -> {
					throw NotImplementedError("Impossible to get data from generic Tag.")
				}
			}
		}

		return primitive
	}

	override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): ListTag {
		val list = ListTag()

		for ((_, entry) in (primitive as? CraftPersistentDataContainer ?: return list).raw) {
			list.add(entry)
		}
		return list
	}
}
