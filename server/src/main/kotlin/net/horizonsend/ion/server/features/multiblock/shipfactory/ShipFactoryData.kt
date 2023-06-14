package net.horizonsend.ion.server.features.multiblock.shipfactory

import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.BLUEPRINT
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.ROTATION
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.RUNNING
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SHIP_FACTORY_DATA
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SHOW_BOUNDING_BOX
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.Y
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.Z
import org.bukkit.block.Sign
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
	var offsetX: Int = 0,
	var offsetY: Int = 0,
	var offsetZ: Int = 0,
	var rotation: Int = 0,
	var blueprintName: String = "",
	var showBoundingBox: Boolean = false,
	var isRunning: Boolean = false,
) {
	fun update(sign: Sign) {
		sign.persistentDataContainer.set(SHIP_FACTORY_DATA, ShipFactoryData, this)
		sign.update()
	}

	companion object : PersistentDataType<PersistentDataContainer, ShipFactoryData> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType(): Class<ShipFactoryData> = ShipFactoryData::class.java

		override fun fromPrimitive(
			primitive: PersistentDataContainer,
			context: PersistentDataAdapterContext,
		): ShipFactoryData {
			val falseByte: Byte = 0

			return ShipFactoryData(
				offsetX = primitive.get(X, INTEGER) ?: 0,
				offsetY = primitive.get(Y, INTEGER) ?: 0,
				offsetZ = primitive.get(Z, INTEGER) ?: 0,
				rotation = primitive.get(ROTATION, INTEGER) ?: 0,
				blueprintName = primitive.get(BLUEPRINT, STRING) ?: "",
				showBoundingBox = ((primitive.get(SHOW_BOUNDING_BOX, BYTE) ?: falseByte) != falseByte),
				isRunning = ((primitive.get(RUNNING, BYTE) ?: falseByte) != falseByte)
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
			new.set(RUNNING, BYTE, (if (complex.isRunning) 1 else 0).toByte())

			return new
		}
	}
}
