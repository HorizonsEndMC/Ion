package net.horizonsend.ion.server.features.space.data

import net.horizonsend.ion.server.IonServer
import net.minecraft.nbt.ByteArrayTag
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntArrayTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.LongArrayTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.ShortTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.BYTE
import org.bukkit.persistence.PersistentDataType.BYTE_ARRAY
import org.bukkit.persistence.PersistentDataType.DOUBLE
import org.bukkit.persistence.PersistentDataType.FLOAT
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.INTEGER_ARRAY
import org.bukkit.persistence.PersistentDataType.LONG
import org.bukkit.persistence.PersistentDataType.LONG_ARRAY
import org.bukkit.persistence.PersistentDataType.SHORT
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.HashMap

object CompoundTagType : PersistentDataType<PersistentDataContainer, CompoundTag> {
	override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
	override fun getComplexType(): Class<CompoundTag> = CompoundTag::class.java

	override fun toPrimitive(complex: CompoundTag, context: PersistentDataAdapterContext): PersistentDataContainer {
		val primitive = context.newPersistentDataContainer()

		for ((name, tag) in complex.tags) {
			val key = NamespacedKey(IonServer, name)

			when (tag) {
				is ByteTag -> primitive.set(key, BYTE, tag.asByte)
				is ShortTag -> primitive.set(key, SHORT, tag.asShort)
				is IntTag -> primitive.set(key, INTEGER, tag.asInt)
				is LongTag -> primitive.set(key, LONG, tag.asLong)
				is FloatTag -> primitive.set(key, FLOAT, tag.asFloat)
				is DoubleTag -> primitive.set(key, DOUBLE, tag.asDouble)
				is ByteArrayTag -> primitive.set(key, BYTE_ARRAY, tag.asByteArray)
				is StringTag -> primitive.set(key, STRING, tag.asString)
				is CompoundTag -> primitive.set(key, CompoundTagType, tag)
				is IntArrayTag -> primitive.set(key, INTEGER_ARRAY, tag.asIntArray)
				is LongArrayTag -> primitive.set(key, LONG_ARRAY, tag.asLongArray)
				else -> throw NotImplementedError("Impossible to get data from generic Tag.")
			}
		}

		return primitive
	}

	override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): CompoundTag {
		val compound = CompoundTag()

		val customDataTagsField = primitive::class.java.getDeclaredField("customDataTags")
		customDataTagsField.isAccessible = true

		@Suppress("UNCHECKED_CAST")
		val customDataTags = customDataTagsField.get(primitive) as HashMap<String, Tag>

		for ((key, tag) in customDataTags) compound.put(key, tag)

		return compound
	}
}
