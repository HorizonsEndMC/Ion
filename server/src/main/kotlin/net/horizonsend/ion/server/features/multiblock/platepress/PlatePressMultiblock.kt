package net.horizonsend.ion.server.features.multiblock.platepress

import net.horizonsend.ion.server.features.customitems.CustomItems.REACTIVE_CHASSIS
import net.horizonsend.ion.server.features.customitems.CustomItems.REACTIVE_PLATING
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


abstract class PlatePressMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).wireInputComputer()
				x(+1).ironBlock()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).machineFurnace()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).goldBlock()
				x(+0).endRod()
				x(+1).goldBlock()
			}
			y(+0) {
				x(-1).ironBlock()
				x(+0).craftingTable()
				x(+1).ironBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).goldBlock()
				x(+0).endRod()
				x(+1).goldBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).pistonBase()
				x(+1).anyGlassPane()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).anyGlass()
				x(+0).sponge()
				x(+1).anyGlass()
				}
			y(+0) {
				x(-1).anyGlass()
				x(+0).anvil()
				x(+1).anyGlass()
			}
		}
		z(+4) {
			y(-1) {
				x(-1).goldBlock()
				x(+0).endRod()
				x(+1).goldBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).pistonBase()
				x(+1).anyGlassPane()
			}
		}
		z(+5) {
			y(-1) {
				x(-1).goldBlock()
				x(+0).endRod()
				x(+1).goldBlock()
			}
			y(+0) {
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
			}
		}
		z(+6) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}


	override val name = "platepress"

	override val signText = createSignText(
			line1 = "&5Plate Press",
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
		if (fuel.customItem == REACTIVE_PLATING) {
			event.isCancelled = false
			fuel.subtract(1)
			if (result == null) furnace.inventory.result = REACTIVE_CHASSIS.constructItemStack()
			else result.add(1)
			PowerMachines.removePower(sign, 300)
		}
	}
}
