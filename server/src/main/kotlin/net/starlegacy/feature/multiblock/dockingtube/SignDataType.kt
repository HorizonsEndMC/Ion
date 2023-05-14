package net.starlegacy.feature.multiblock.dockingtube

import com.manya.pdc.minecraft.LocationDataType
import net.horizonsend.ion.common.IntLocation
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class SignDataType(val loc: IntLocation, val type: Material) {
	companion object : PersistentDataType<PersistentDataContainer, SignDataType> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = SignDataType::class.java

		override fun toPrimitive(
			complex: SignDataType,
			context: PersistentDataAdapterContext
		): PersistentDataContainer {
			val primitive = context.newPersistentDataContainer()
			val (x, y, z) = complex.loc

			primitive.set(NamespacedKeys.X, PersistentDataType.INTEGER, x)
			primitive.set(NamespacedKeys.Y, PersistentDataType.INTEGER, y)
			primitive.set(NamespacedKeys.Z, PersistentDataType.INTEGER, z)

			primitive.set(NamespacedKeys.MATERIAL, PersistentDataType.STRING, complex.type.name)

			return primitive
		}

		override fun fromPrimitive(
			primitive: PersistentDataContainer,
			context: PersistentDataAdapterContext
		): SignDataType {
			val x = primitive.get(NamespacedKeys.X, PersistentDataType.INTEGER)!!
			val y = primitive.get(NamespacedKeys.Y, PersistentDataType.INTEGER)!!
			val z = primitive.get(NamespacedKeys.Z, PersistentDataType.INTEGER)!!

			val material = Material.getMaterial(primitive.get(NamespacedKeys.MATERIAL, PersistentDataType.STRING)!!)!!

			return SignDataType(IntLocation(x, y, z), material)
		}
	}
}
