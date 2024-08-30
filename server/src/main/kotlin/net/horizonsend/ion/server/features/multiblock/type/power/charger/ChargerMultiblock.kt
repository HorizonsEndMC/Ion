package net.horizonsend.ion.server.features.multiblock.type.power.charger

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.PowerStorage
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

abstract class ChargerMultiblock(val tierText: String) : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	protected abstract val tierMaterial: Material

	protected abstract val powerPerSecond: Int

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
				x(+1).anyGlassPane()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).type(tierMaterial)
				x(+1).anyGlassPane()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).type(tierMaterial)
				x(+1).anyGlassPane()
			}
		}
	}

	override val name = "charger"

	override val signText = createSignText(
		line1 = "&6Item",
		line2 = "&8Charger",
		line3 = null,
		line4 = tierText
	)

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		event.isBurning = false
		event.burnTime = 0
		val inventory = furnace.inventory
		val smelting = inventory.smelting
		val power = PowerMachines.getPower(sign)
		val item = event.fuel
		if (smelting == null || smelting.type != Material.PRISMARINE_CRYSTALS) {
			return
		}
		if (power == 0) {
			return
		}

		val custom = item.customItem ?: return
		if (custom.hasComponent(CustomComponentTypes.POWER_STORAGE))
			handleModern(item, custom, custom.getComponent(CustomComponentTypes.POWER_STORAGE), event, furnace, inventory, sign, power)
	}

	fun handleModern(
		item: ItemStack,
		customItem: CustomItem,
		powerManager: PowerStorage,
		event: FurnaceBurnEvent,
		furnace: Furnace,
		inventory: FurnaceInventory,
		sign: Sign,
		power: Int
	) {
		if (powerManager.getMaxPower(customItem, item) == powerManager.getPower(item)) {
			val result = inventory.result
			if (result != null && result.type != Material.AIR) return
			inventory.result = event.fuel
			inventory.fuel = null
			return
		}

		var multiplier = powerPerSecond
		multiplier /= item.amount

		if (item.amount * multiplier > power) return

		powerManager.addPower(item, customItem, multiplier)

		PowerMachines.setPower(sign, power - multiplier * item.amount)

		furnace.cookTime = 20.toShort()
		event.isCancelled = false

		val fuel = checkNotNull(inventory.fuel)

		event.isBurning = false
		event.burnTime = 20
	}
}
