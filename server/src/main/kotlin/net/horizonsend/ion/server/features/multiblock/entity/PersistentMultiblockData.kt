package net.horizonsend.ion.server.features.multiblock.entity

import kotlinx.serialization.SerializationException
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.ADDITIONAL_MULTIBLOCK_DATA
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Y
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Z
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.STRING
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER

class PersistentMultiblockData(val x: Int, val y: Int, val z: Int, val type: Multiblock) {
	constructor(x: Int, y: Int, z: Int, type: Multiblock, additionalData: PersistentDataContainer) : this(x, y, z, type) {
		this.additionalData = additionalData
	}

	private var additionalData: PersistentDataContainer = CraftPersistentDataContainer(CraftPersistentDataTypeRegistry())

	fun <Z : Any> addAdditionalData(key: NamespacedKey, type: PersistentDataType<*, Z>, value: Z) = additionalData.set(key, type, value)
	fun <Z: Any> getAdditionalData(key: NamespacedKey, type: PersistentDataType<*, Z>) = additionalData.get(key, type)

	companion object : PersistentDataType<PersistentDataContainer, PersistentMultiblockData> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = PersistentMultiblockData::class.java

		override fun toPrimitive(complex: PersistentMultiblockData, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()

			pdc.set(X, INTEGER, complex.x)
			pdc.set(Y, INTEGER, complex.y)
			pdc.set(Z, INTEGER, complex.z)

			pdc.set(MULTIBLOCK, STRING, complex.type::class.java.simpleName)

			pdc.set(ADDITIONAL_MULTIBLOCK_DATA, TAG_CONTAINER, complex.additionalData)

			return pdc
		}

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): PersistentMultiblockData = runCatching {
			val x = primitive.get(X, INTEGER)!!
			val y = primitive.get(Y, INTEGER)!!
			val z = primitive.get(Z, INTEGER)!!

			val multiblockType = Multiblocks.all().firstOrNull { it::class.simpleName == primitive.get(MULTIBLOCK, STRING) }!!

			val additionalData = primitive.get(ADDITIONAL_MULTIBLOCK_DATA, TAG_CONTAINER)!!

			return@runCatching PersistentMultiblockData(x, y, z, multiblockType, additionalData)
		}.getOrNull() ?: throw SerializationException("Error deserializing multiblock data!")
	}
}
