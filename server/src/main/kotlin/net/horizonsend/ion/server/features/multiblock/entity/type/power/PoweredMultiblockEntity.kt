package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataType

interface PoweredMultiblockEntity {
	val poweredMultiblock: NewPoweredMultiblock<*>
	val powerStorage: PowerStorage

	fun loadStoredPower(data: PersistentMultiblockData): PowerStorage {
		return PowerStorage(this, data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0), poweredMultiblock.maxPower)
	}

	fun savePowerData(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, powerStorage.getPower())
	}
}
