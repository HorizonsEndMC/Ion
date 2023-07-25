package net.horizonsend.ion.server.features.multiblock.dockingtube

import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class StoredButtonDataType(val leftRight: Int, val upDown: Int, val type: Material) {
	companion object : PersistentDataType<PersistentDataContainer, StoredButtonDataType> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = StoredButtonDataType::class.java

		override fun toPrimitive(
			complex: StoredButtonDataType,
			context: PersistentDataAdapterContext
		): PersistentDataContainer {
			val primitive = context.newPersistentDataContainer()

			primitive.set(NamespacedKeys.LEFT_RIGHT, PersistentDataType.INTEGER, complex.leftRight)
			primitive.set(NamespacedKeys.UP_DOWN, PersistentDataType.INTEGER, complex.upDown)

			primitive.set(NamespacedKeys.MATERIAL, PersistentDataType.STRING, complex.type.name)

			return primitive
		}

		override fun fromPrimitive(
			primitive: PersistentDataContainer,
			context: PersistentDataAdapterContext
		): StoredButtonDataType {
			val leftRight = primitive.get(NamespacedKeys.LEFT_RIGHT, PersistentDataType.INTEGER)!!
			val upDown = primitive.get(NamespacedKeys.UP_DOWN, PersistentDataType.INTEGER)!!

			val material = Material.getMaterial(primitive.get(NamespacedKeys.MATERIAL, PersistentDataType.STRING)!!)!!

			return StoredButtonDataType(leftRight, upDown, material)
		}
	}
}
