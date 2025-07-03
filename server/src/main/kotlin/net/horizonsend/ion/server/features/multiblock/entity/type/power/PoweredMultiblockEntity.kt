package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataType

interface PoweredMultiblockEntity {
	val powerStorage: PowerStorage
	val maxPower: Int

	fun loadStoredPower(data: PersistentMultiblockData): PowerStorage {
		return PowerStorage(this, data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0), maxPower)
	}

	fun savePowerData(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, powerStorage.getPower())
	}

	/**
	 * Registers power inputs on all blocks adjacent to the sign location
	 **/
	fun IOData.Builder.registerSignInputs(): IOData.Builder {
		addPowerInput(0, -1, -1)
		addPowerInput(0, 1, -1)
		addPowerInput(1, 0, -1)
		addPowerInput(-1, 0, -1)
		addPowerInput(0, 0, -2)
		return this
	}
}
