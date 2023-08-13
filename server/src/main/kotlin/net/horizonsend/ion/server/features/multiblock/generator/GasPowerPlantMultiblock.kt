package net.horizonsend.ion.server.features.multiblock.generator

import net.horizonsend.ion.server.features.gas.Fuels
import net.horizonsend.ion.server.features.gas.GasProperties
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Effect
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

object GasPowerPlantMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower: Int = 500000

	override val name: String = "gaspowerplant"

	override val signText: Array<Component?> = arrayOf(
		text("Gas Power Plant", NamedTextColor.RED),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		at(x = -1, y = -1, z = +0).extractor()
		at(x = +0, y = -1, z = +0).wireInputComputer()
		at(x = +1, y = -1, z = +0).extractor()
		at(x = +0, y = +0, z = +0).machineFurnace()
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 0
		val inventory = furnace.inventory

		println(0)

		val top = inventory.smelting ?: return
		val bottom = inventory.fuel ?: return

		println(1)

		val topCustomItem = CustomItems[top] ?: return
		println(1.1)
		val bottomCustomItem = CustomItems[bottom] ?: return

		println(1.2)

		println(2)

		val oxidizerProperties = GasProperties[bottomCustomItem.id] ?: return
		val fuelProperties = GasProperties[topCustomItem.id] ?: return

		println(3)

		val power = fuelProperties.getPower(oxidizerProperties)
		val cooldown = (fuelProperties as Fuels).cooldown

		println(4)

		if (PowerMachines.getPower(sign) < this.maxPower) {
			event.isBurning = true
			event.burnTime = cooldown
			furnace.cookTime = (-1000).toShort()

			PowerMachines.addPower(sign, power)

			return
		} else {
			furnace.world.playEffect(furnace.location.add(0.5, 0.5, 0.5), Effect.SMOKE, 4)
		}
		event.isCancelled = true
	}
}
