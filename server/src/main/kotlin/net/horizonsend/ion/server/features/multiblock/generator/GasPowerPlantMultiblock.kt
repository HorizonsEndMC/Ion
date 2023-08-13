package net.horizonsend.ion.server.features.multiblock.generator

import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.machine.GeneratorFuel
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Effect
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

object GasPowerPlantMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower: Int = 500000

	override val name: String = "gaspowerplant"

	override val signText: Array<Component?> = arrayOf(
		text("Gas Power Plant"),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		TODO("Not yet implemented")
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 0
		val inventory = furnace.inventory

		val smelting = inventory.smelting ?: return
		val fuel = inventory.fuel ?: return

		val gasItem1 = CustomItems[smelting] ?: return
		val gasItem2 = CustomItems[fuel] ?: return
		if (gasItem1 !is CustomItems.GasItem || gasItem2 !is CustomItems.GasItem) return

		val gas1 = Gasses[gasItem1.id] ?: return
		val gas2 = Gasses[gasItem2.id] ?: return



		if (PowerMachines.getPower(sign) < this.maxPower) {
			val fuelItem = inventory.fuel ?: return
			val fuel = GeneratorFuel.getFuel(fuelItem) ?: return

			event.isBurning = true
			event.burnTime = (fuel.cooldown / speed).toInt()
			furnace.cookTime = (-1000).toShort()

			PowerMachines.addPower(sign, fuel.power)

			return
		} else {
			furnace.world.playEffect(furnace.location.add(0.5, 0.5, 0.5), Effect.SMOKE, 4)
		}
		event.isCancelled = true
	}
}
