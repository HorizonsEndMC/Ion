package net.horizonsend.ion.server.features.multiblock.circuitfab

import net.horizonsend.ion.server.features.customitems.CustomItems.CIRCUITRY
import net.horizonsend.ion.server.features.customitems.CustomItems.CIRCUIT_BOARD
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Item
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.ItemStack


abstract class CircuitfabMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).wireInputComputer()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).craftingTable()
				x(+0).machineFurnace()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).redstoneBlock()
				x(+0).redstoneBlock()
				x(+1).redstoneBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).ironBlock()
				x(-1).endRod()
				x(+0).anvil()
				x(+1).endRod()
				x(+2).ironBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).titaniumBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).titaniumBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
		}
	}


	override val name = "circuitfab"

	override val signText = createSignText(
			line1 = "&6Circuitfab",
			line2 = null,
			line3 = null,
			line4 = null
	)

	override fun onFurnaceTick(
			event: FurnaceBurnEvent,
			furnace: Furnace,
			sign: Sign
	) {
		event.isBurning = false
		event.burnTime = 3600000
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false

		val smelting = furnace.inventory.smelting
		val fuel = furnace.inventory.fuel
		val result = furnace.inventory.result

		if (PowerMachines.getPower(sign) == 0 ||
				smelting == null ||
				smelting.type != Material.PRISMARINE_CRYSTALS ||
				fuel == null
		) {
			return
		}
		if (fuel.customItem != CIRCUITRY) return
		event.isCancelled = false
		fuel.subtract(1)
		if (result == null)  furnace.inventory.result = CIRCUIT_BOARD.constructItemStack()
		else result.add(1)
		PowerMachines.removePower(sign, 300)
	}
}
