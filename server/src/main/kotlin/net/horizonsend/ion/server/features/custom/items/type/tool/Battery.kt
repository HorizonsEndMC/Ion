package net.horizonsend.ion.server.features.custom.items.type.tool

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener
import net.horizonsend.ion.server.features.custom.items.component.PowerStorage
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.Sign
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class Battery(type: Char, color: TextColor, maxPower: Int) : CustomItem(
	identifier = "BATTERY_$type",
	displayName = ofChildren(text("Size", BLUE), text("-", GRAY), text("$type", color), text(" Battery", BLUE)),
	baseItemFactory = ItemFactory.stackableCustomItem(maxStackSize = 16, "battery/battery_${type.toString().lowercase()}")
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.POWER_STORAGE, PowerStorage(maxPower, 0, true))
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, Listener.rightClickListener(this@Battery) { event, _, item ->
			tryDepositPower(event, item)
		})
	}

	fun tryDepositPower(event: PlayerInteractEvent, item: ItemStack) {
		val clickedBlock = event.clickedBlock ?: return
		val clickedType = clickedBlock.type
		if (!clickedType.isWallSign) return

		val sign = event.clickedBlock?.state as? Sign ?: return
		val multiblock = MultiblockAccess.getFast(sign) ?: return
		if (multiblock !is EntityMultiblock<*>) return

		val entity = multiblock.getMultiblockEntity(sign) ?: return
		if (entity !is PoweredMultiblockEntity) return

		handleBatteryInput(entity, event)
	}

	private fun handleBatteryInput(entity: PoweredMultiblockEntity, event: PlayerInteractEvent) {
		val item = event.item ?: return

		val power = getComponent(CustomComponentTypes.POWER_STORAGE).getPower(item)
		var powerToTransfer = power * item.amount
		if (powerToTransfer == 0) return

		val machinePower = entity.powerStorage.getPower()
		val maxMachinePower = entity.powerStorage.capacity

		if (maxMachinePower - machinePower < powerToTransfer) {
			powerToTransfer = maxMachinePower - machinePower
		}

		getComponent(CustomComponentTypes.POWER_STORAGE).setPower(this, item, power - powerToTransfer / item.amount)
		entity.powerStorage.addPower(powerToTransfer)
	}
}
