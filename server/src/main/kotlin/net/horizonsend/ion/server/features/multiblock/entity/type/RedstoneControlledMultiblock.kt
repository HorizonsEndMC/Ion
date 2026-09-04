package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.util.ControlSignalManager
import net.horizonsend.ion.server.gui.invui.utils.buttons.BuildableCycleButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.invui.item.Item
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

	enum class ControlMode(val displayName: Component, val icon: GuiItem, val description: String) {
		DISABLED(Component.text("Disabled"), GuiItem.CANCEL, "The multiblock will run regardless of redstone state.") {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = true
		},
		DIRECT_SIGNAL_REQUIRED(Component.text("Direct Signal Required"), GuiItem.DOWN, "The multiblock will only run if any redstone control port is directly powered.") {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = input.hasAnyDirectPower()
		},
		INDIRECT_SIGNAL_REQUIRED(Component.text("Indirect Signal Required"), GuiItem.RIGHT, "The multiblock will only run if any redstone control port is indirectly powered.") {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = input.hasAnyIndirectPower()
		},
		DIRECT_SIGNAL_DISABLES(Component.text("Direct Signal Disables"), GuiItem.DOWN, "The multiblock will not run if any redstone control port is directly powered.") {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = !input.hasAnyDirectPower()
		},
		INDIRECT_SIGNAL_DISABLES(Component.text("Indirect Signal Disables"), GuiItem.LEFT, "The multiblock will not run if any redstone control port is indirectly powered.") {
			override fun checkRedstoneSignal(input: ControlSignalManager): Boolean = !input.hasAnyIndirectPower()
		};

		/**
		 * Returns if redstone signal matches the requirement
		 **/
		abstract fun checkRedstoneSignal(input: ControlSignalManager): Boolean

		fun getButton(entity: RedstoneControlledMultiblock): Item = icon
			.makeItem(displayName)
			.updateLore(Component.text(description).wrap(180))
			.makeGuiButton { _, _ -> entity.controlMode = this }

		companion object {
			operator fun get(index: Int): ControlMode = entries[index]

			val storageKey = NamespacedKeys.key("control_mode")

			fun getCycleButton(entity: RedstoneControlledMultiblock): CycleItem {
				val button = BuildableCycleButton.builder()

				for (mode in entries) {
					button.addButton(mode.getButton(entity))
				}

				return button.build(startState = entity.controlMode.ordinal)
			}
		}
	}
}
