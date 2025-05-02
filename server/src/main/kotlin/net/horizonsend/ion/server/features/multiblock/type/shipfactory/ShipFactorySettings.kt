package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.ROTATION
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Y
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Z
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.key
import org.bukkit.persistence.PersistentDataType.BOOLEAN
import org.bukkit.persistence.PersistentDataType.INTEGER

data class ShipFactorySettings(
	var offsetX: Int,
	var offsetY: Int,
	var offsetZ: Int,
	var rotation: Int,
	// Placement
	var markObstrcutedBlocksAsComplete: Boolean,
	var overrideReplaceableBlocks: Boolean,
	var placeBlocksUnderwater: Boolean,
	// Item
	var leaveItemRemaining: Boolean,
	var grabFromNetworkedPipes: Boolean
) {
	companion object {
		val COMPLETE_OBSTRUCTIONS = key("complete_obstructions")
		val OVERRIDE_REPLACEABLE = key("override_replaceable")
		val PLACE_UNDERWATER = key("place_underwater")
		val LEAVE_ITEM_REMAINING = key("leave_item_remaining")
		val GRAB_NETWORKED_INVENTORIES = key("grab_networked_inventories")

		fun load(data: PersistentMultiblockData): ShipFactorySettings {
			val shipFactorySettings = ShipFactorySettings(
				offsetX = data.getAdditionalDataOrDefault(X, INTEGER, 0),
				offsetY = data.getAdditionalDataOrDefault(Y, INTEGER, 0),
				offsetZ = data.getAdditionalDataOrDefault(Z, INTEGER, 0),
				rotation = data.getAdditionalDataOrDefault(ROTATION, INTEGER, 0),
				markObstrcutedBlocksAsComplete = data.getAdditionalDataOrDefault(COMPLETE_OBSTRUCTIONS, BOOLEAN, true),
				overrideReplaceableBlocks = data.getAdditionalDataOrDefault(OVERRIDE_REPLACEABLE, BOOLEAN, true),
				placeBlocksUnderwater = data.getAdditionalDataOrDefault(PLACE_UNDERWATER, BOOLEAN, false),
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
		store.addAdditionalData(ROTATION, INTEGER, rotation)
		store.addAdditionalData(COMPLETE_OBSTRUCTIONS, BOOLEAN, markObstrcutedBlocksAsComplete)
		store.addAdditionalData(PLACE_UNDERWATER, BOOLEAN, placeBlocksUnderwater)
		store.addAdditionalData(OVERRIDE_REPLACEABLE, BOOLEAN, overrideReplaceableBlocks)
		store.addAdditionalData(LEAVE_ITEM_REMAINING, BOOLEAN, leaveItemRemaining)
		store.addAdditionalData(GRAB_NETWORKED_INVENTORIES, BOOLEAN, grabFromNetworkedPipes)
	}
}

