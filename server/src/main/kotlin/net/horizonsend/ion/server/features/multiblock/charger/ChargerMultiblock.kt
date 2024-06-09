package net.horizonsend.ion.server.features.multiblock.charger

import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.powered.PoweredItem
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.misc.addPower
import net.horizonsend.ion.server.features.misc.getMaxPower
import net.horizonsend.ion.server.features.misc.getPower
import net.horizonsend.ion.server.features.misc.isPowerable
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
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
		if (isPowerable(item)) {
			handleLegacy(item, event, furnace, inventory, sign, power)
		}

		val custom = item.customItem

		if (custom is PoweredItem) {
			handleModern(item, custom, event, furnace, inventory, sign, power)
		}
	}

	fun handleLegacy(
		item: ItemStack,
		event: FurnaceBurnEvent,
		furnace: Furnace,
		inventory: FurnaceInventory,
		sign: Sign,
		power: Int
	) {
		if (getMaxPower(item) == getPower(item)) {
			val result = inventory.result
			if (result != null && result.type != Material.AIR) return
			inventory.result = event.fuel
			inventory.fuel = null
			return
		}
		var multiplier = powerPerSecond
		multiplier /= item.amount
		if (item.amount * multiplier > power) return
		addPower(item, multiplier)
		PowerMachines.setPower(sign, power - multiplier * item.amount)
		furnace.cookTime = 20.toShort()
		event.isCancelled = false
		val fuel = checkNotNull(inventory.fuel)
		event.isBurning = false
		event.burnTime = 20
	}

	fun handleModern(
		item: ItemStack,
		customItem: PoweredItem,
		event: FurnaceBurnEvent,
		furnace: Furnace,
		inventory: FurnaceInventory,
		sign: Sign,
		power: Int
	) {
		if (customItem.getPowerCapacity(item) == customItem.getPower(item)) {
			val result = inventory.result
			if (result != null && result.type != Material.AIR) return
			inventory.result = event.fuel
			inventory.fuel = null
			return
		}

		var multiplier = powerPerSecond
		multiplier /= item.amount

		if (item.amount * multiplier > power) return

		customItem.addPower(item, multiplier)

		PowerMachines.setPower(sign, power - multiplier * item.amount)

		furnace.cookTime = 20.toShort()
		event.isCancelled = false

		val fuel = checkNotNull(inventory.fuel)

		event.isBurning = false
		event.burnTime = 20
	}
}
