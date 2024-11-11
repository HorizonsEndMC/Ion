package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.persistence.PersistentDataType

interface PoweredMultiblockEntity {
	val powerStorage: PowerStorage
	val maxPower: Int

	val powerInputOffsets: Array<Vec3i>

	fun getPowerInputLocations(): Set<Vec3i> {
		this as MultiblockEntity
		return powerInputOffsets.mapTo(mutableSetOf()) { (right, up, forward) -> getPosRelative(right, up, forward) }
	}

	fun loadStoredPower(data: PersistentMultiblockData): PowerStorage {
		return PowerStorage(this, data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0), maxPower)
	}

	fun savePowerData(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, powerStorage.getPower())
	}
}
