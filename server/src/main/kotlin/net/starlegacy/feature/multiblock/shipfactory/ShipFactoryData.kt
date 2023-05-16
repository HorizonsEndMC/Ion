package net.starlegacy.feature.multiblock.shipfactory

import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.BLUEPRINT
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.ROTATION
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SHOW_BOUNDING_BOX
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.Y
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.Z
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.BYTE
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.STRING

/**
 *
 *
 * @param rotation multiples of 90
 **/
data class ShipFactoryData(
	val offsetX: Int = 0,
	val offsetY: Int = 0,
	val offsetZ: Int = 0,
	val rotation: Int = 0,
	val blueprintName: String = "",
	val showBoundingBox: Boolean = false,
) {
	companion object : PersistentDataType<PersistentDataContainer, ShipFactoryData> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType(): Class<ShipFactoryData> = ShipFactoryData::class.java

		override fun fromPrimitive(
			primitive: PersistentDataContainer,
			context: PersistentDataAdapterContext,
		): ShipFactoryData {
			val trueByte: Byte = 1

			return ShipFactoryData(
				offsetX = primitive.get(X, INTEGER)!!,
				offsetY = primitive.get(Y, INTEGER)!!,
				offsetZ = primitive.get(Z, INTEGER)!!,
				rotation = primitive.get(ROTATION, INTEGER)!!,
				blueprintName = primitive.get(BLUEPRINT, STRING)!!,
				showBoundingBox = (primitive.get(SHOW_BOUNDING_BOX, BYTE)!! == trueByte)
			)
		}

		override fun toPrimitive(
			complex: ShipFactoryData,
			context: PersistentDataAdapterContext,
		): PersistentDataContainer {
			val new = context.newPersistentDataContainer()

			new.set(X, INTEGER, complex.offsetX)
			new.set(Y, INTEGER, complex.offsetY)
			new.set(Z, INTEGER, complex.offsetZ)
			new.set(ROTATION, INTEGER, complex.rotation)
			new.set(BLUEPRINT, STRING, complex.blueprintName)
			new.set(SHOW_BOUNDING_BOX, BYTE, (if (complex.showBoundingBox) 1 else 0).toByte())

			return new
		}
	}
}
