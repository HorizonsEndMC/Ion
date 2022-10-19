package net.starlegacy.feature.multiblock.charger

import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.misc.addPower
import net.starlegacy.feature.misc.getMaxPower
import net.starlegacy.feature.misc.getPower
import net.starlegacy.feature.misc.isPowerable
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

abstract class ChargerMultiblock(val tierText: String) : PowerStoringMultiblock(), FurnaceMultiblock {
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
		if (!isPowerable(item)) {
			return
		}
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
}
