package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.util.ControlSignalManager
import net.horizonsend.ion.server.gui.invui.utils.buttons.BuildableCycleButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.invui.item.impl.CycleItem

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

	enum class ControlMode(val displayName: Component, val icon: GuiItem) {
		DISABLED(Component.text("Disabled"), GuiItem.CANCEL) {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = true
		},
		STRONG_SIGNAL_REQUIRED(Component.text("Strong Signal Required"), GuiItem.DOWN) {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = input.hasAnyDirectPower()
		},
		WEAK_SIGNAL_REQUIRED(Component.text("Weak Signal Required"), GuiItem.RIGHT) {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = input.hasAnyIndirectPower()
		},
		STRONG_SIGNAL_DISABLES(Component.text("Strong Signal Disables"), GuiItem.DOWN) {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = !input.hasAnyDirectPower()
		},
		WEAK_SIGNAL_DISABLES(Component.text("Weak Signal Disables"), GuiItem.LEFT) {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = !input.hasAnyIndirectPower()
		};

		/**
		 * Returns if redstone signal matches the requirement
		 **/
		abstract fun checkRedstoneSignal(input: ControlSignalManager): Boolean

		companion object {
			operator fun get(index: Int): ControlMode = entries[index]

			val storageKey = NamespacedKeys.key("control_mode")

			fun getCycleButton(entity: RedstoneControlledMultiblock): CycleItem {
				val button = BuildableCycleButton.builder()

				for (mode in entries) {
					button.addButton(mode.icon.makeItem(mode.displayName).makeGuiButton { _, _ -> entity.controlMode = mode })
				}

				return button.build(startState = entity.controlMode.ordinal)
			}
		}
	}
}
