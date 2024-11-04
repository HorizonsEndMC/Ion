package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.PoweredMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataType

interface PoweredMultiblockEntity {
	val powerStorage: PowerStorage

	fun loadStoredPower(data: PersistentMultiblockData): PowerStorage {
		val multiblock = (this as MultiblockEntity).multiblock as PoweredMultiblock<*>
		return PowerStorage(this, data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0), multiblock.maxPower)
	}

	fun savePowerData(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, powerStorage.getPower())
	}
}
