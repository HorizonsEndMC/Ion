package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.COMPLETE_OBSTRUCTIONS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FLUID_PACEMENT
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.GRAB_NETWORKED_INVENTORIES
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.LEAVE_ITEM_REMAINING
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.ROTATION
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Y
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Z
import net.minecraft.world.level.block.Rotation
import org.bukkit.persistence.PersistentDataType.BOOLEAN
import org.bukkit.persistence.PersistentDataType.INTEGER

data class ShipFactorySettings(
	var offsetX: Int,
	var offsetY: Int,
	var offsetZ: Int,
	var rotation: Rotation,
	var placeInFluids: Boolean,
	var markObstrcutedBlocksAsComplete: Boolean,
	var leaveItemRemaining: Boolean,
	var grabFromNetworkedPipes: Boolean
) {
	companion object {
		private val enumType = EnumDataType(Rotation::class.java)

		fun load(data: PersistentMultiblockData): ShipFactorySettings {
			val shipFactorySettings = ShipFactorySettings(
				offsetX = data.getAdditionalDataOrDefault(X, INTEGER, 0),
				offsetY = data.getAdditionalDataOrDefault(Y, INTEGER, 0),
				offsetZ = data.getAdditionalDataOrDefault(Z, INTEGER, 0),
				rotation = data.getAdditionalDataOrDefault(ROTATION, enumType, Rotation.NONE),
				placeInFluids = data.getAdditionalDataOrDefault(FLUID_PACEMENT, BOOLEAN, false),
				markObstrcutedBlocksAsComplete = data.getAdditionalDataOrDefault(COMPLETE_OBSTRUCTIONS, BOOLEAN, true),
				leaveItemRemaining = data.getAdditionalDataOrDefault(LEAVE_ITEM_REMAINING, BOOLEAN, false),
				grabFromNetworkedPipes = data.getAdditionalDataOrDefault(GRAB_NETWORKED_INVENTORIES, BOOLEAN, false),
			)

			return shipFactorySettings
		}
	}

	fun save(store: PersistentMultiblockData) {
		store.addAdditionalData(X, INTEGER, offsetX)
		store.addAdditionalData(Y, INTEGER, offsetY)
		store.addAdditionalData(Z, INTEGER, offsetZ)
		store.addAdditionalData(ROTATION, enumType, rotation)
		store.addAdditionalData(FLUID_PACEMENT, BOOLEAN, placeInFluids)
		store.addAdditionalData(COMPLETE_OBSTRUCTIONS, BOOLEAN, markObstrcutedBlocksAsComplete)
		store.addAdditionalData(LEAVE_ITEM_REMAINING, BOOLEAN, leaveItemRemaining)
		store.addAdditionalData(GRAB_NETWORKED_INVENTORIES, BOOLEAN, grabFromNetworkedPipes)
	}
}

