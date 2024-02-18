package net.horizonsend.ion.server.features.multiblock.fabricator

import net.horizonsend.ion.server.features.customitems.CustomItems.ENRICHED_URANIUM
import net.horizonsend.ion.server.features.customitems.CustomItems.FUEL_CELL
import net.horizonsend.ion.server.features.customitems.CustomItems.FUEL_ROD_CORE
import net.horizonsend.ion.server.features.customitems.CustomItems.URANIUM
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.ItemStack


abstract class FabricatorMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).wireInputComputer()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+1) {
				x(-2).anyStairs()
				x(-1).craftingTable()
				x(+0).machineFurnace()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
		z(+1) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).aluminumBlock()
				x(+0).aluminumBlock()
				x(+1).aluminumBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).anyGlassPane()
				x(+2).anyGlassPane()
			}
		z(+2) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).aluminumBlock()
				x(+0).sculkCatalyst()
				x(+1).aluminumBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).anyGlass()
				x(-1).endRod()
				x(+0).anvil()
				x(+1).endRod()
				x(+2).anyGlass()
			}
		z(+3) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).aluminumBlock()
				x(+0).aluminumBlock()
				x(+1).aluminumBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).anyGlassPane()
				x(+2).anyGlassPane()
			}
		z(+4) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).anyGlass()
				x(+0).anyGlass()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
		}
		}
		}
		}
		}

		}

	override val name = "fabricator"

	override val signText = createSignText(
			line1 = "&7Fabricator",
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
		event.burnTime = 360000
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
		if (fuel.customItem == FUEL_ROD_CORE) {
			event.isCancelled = false
			fuel.subtract(1)
			if (result == null) furnace.inventory.result = FUEL_CELL.constructItemStack()
			else result.add(1)
			PowerMachines.removePower(sign, 300)
		}
	}
}
