package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.util.ControlSignalManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataType

interface RedstoneControlledMultiblock {
	val primaryControlInputs: ControlSignalManager
	var controlMode: ControlMode

	fun saveControlMode(destination: PersistentMultiblockData) {
		destination.addAdditionalData(ControlMode.storageKey, PersistentDataType.INTEGER, controlMode.ordinal)
	}

	fun loadControlMode(data: PersistentMultiblockData): ControlMode = ControlMode[data.getAdditionalDataOrDefault(ControlMode.storageKey, PersistentDataType.INTEGER, 0)]

	/**
	 * Returns if redstone signal matches the requirement
	 **/
	fun isRedstoneEnabled(): Boolean {
		return controlMode.checkRedstoneSignal(primaryControlInputs)
	}

	enum class ControlMode {
		DISABLED {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = true
		},
		STRONG_SIGNAL_REQUIRED {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = input.hasAnyDirectPower()
		},
		WEAK_SIGNAL_REQUIRED {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = input.hasAnyIndirectPower()
		},
		STRONG_SIGNAL_DISABLES {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = !input.hasAnyDirectPower()
		},
		WEAK_SIGNAL_DISABLES {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = !input.hasAnyIndirectPower()
		};

		/**
		 * Returns if redstone signal matches the requirement
		 **/
		abstract fun checkRedstoneSignal(input: ControlSignalManager): Boolean

		companion object {
			operator fun get(index: Int): ControlMode = entries[index]

			val storageKey = NamespacedKeys.key("control_mode")
		}
	}
}
