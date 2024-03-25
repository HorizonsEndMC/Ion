package net.horizonsend.ion.server.features.multiblock.refinery

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


//abstract class RefineryMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
//	override fun MultiblockShape.buildStructure() {
//		z(+0) {
//			y(-1) {
//				x(-2).anyStairs()
//				x(-1).ironBlock()
//				x(+0).wireInputComputer()
//				x(+1).ironBlock()
//				x(+2).anyStairs()
//			}
//			y(+0) {
//				x(-2).ironBlock()
//				x(-1).craftingTable()
//			x(+0).machineFurnace()
//				x(+1).anyGlass()
//				x(+2).ironBlock()
//			}
//			y(+1) {
//				x(-2).anyStairs()
//				x(-1).ironBlock()
//				x(+0).ironBlock()
//				x(+1).ironBlock()
//				x(+2).anyStairs()
//			}
//		}
//		z(+1) {
//			y(-1) {
//				x(-2).aluminumBlock()
//				x(-1).anyGlass()
//				x(+0).anyGlass()
//				x(+1).anyGlass()
//				x(+2).aluminumBlock()
//			}
//			y(+0) {
//				x(-2).sponge()
//				x(-1).lightningRod()
//				x(+0).lightningRod()
//				x(+1).lightningRod()
//				x(+2).sponge()
//			}
//			y(+1) {
//				x(-2).aluminumBlock()
//				x(-1).anyGlass()
//				x(+0).anyGlass()
//				x(+1).anyGlass()
//				x(+2).aluminumBlock()
//			}
//		}
//		z(+2) {
//			y(-1) {
//				x(-2).aluminumBlock()
//				x(-1).anyGlass()
//				x(+0).anyGlass()
//				x(+1).anyGlass()
//				x(+2).aluminumBlock()
//			}
//			y(+0) {
//				x(-2).sponge()
//				x(-1).copperBlock()
//				x(+0).copperBlock()
//				x(+1).copperBlock()
//				x(+2).sponge()
//			}
//			y(+1) {
//				x(-2).aluminumBlock()
//				x(-1).anyGlass()
//				x(+0).anyGlass()
//				x(+1).anyGlass()
//				x(-2).aluminumBlock()
//			}
//		}
//		z(+3) {
//			y(-1) {
//				x(-2).aluminumBlock()
//				x(-1).anyGlass()
//				x(+0).anyGlass()
//				x(+1).anyGlass()
//				x(+2).aluminumBlock()
//			}
//			y(+0) {
//				x(-2).sponge()
//				x(-1).lightningRod()
//				x(+0).lightningRod()
//				x(+1).lightningRod()
//				x(+2).sponge()
//			}
//			y(+1) {
//				x(-2).aluminumBlock()
//				x(-1).anyGlass()
//				x(+0).anyGlass()
//				x(+1).anyGlass()
//				x(+2).aluminumBlock()
//			}
//		}
//		z(+4) {
//			y(-1) {
//				x(-2).anyStairs()
//				x(-1).ironBlock()
//				x(+0).anyGlass()
//				x(+1).ironBlock()
//				x(+2).anyStairs()
//			}
//			y(+0) {
//				x(-2).ironBlock()
//				x(-1).ironBlock()
// 			x(+0).anyGlass()
//			x(+1).ironBlock()
// 			x(+2).ironBlock()
//		}
//			y(+1) {
//				x(-2).anyStairs()
//				x(-1).ironBlock()
//				x(+0).anyGlass()
//				x(+1).ironBlock()
//				x(+2).anyStairs()
//			}
//		}
//	}
//

//	override val name = "refinery"
//
//	override val signText = createSignText(
//			line1 = "&6Refinery",
//			line2 = null,
//			line3 = null,
//			line4 = null
//	)
//
//	override fun onFurnaceTick(
//			event: FurnaceBurnEvent,
//			furnace: Furnace,
//			sign: Sign
//	) {
//		event.isBurning = false
//		event.burnTime = 200
//		event.isCancelled = false
//		furnace.cookSpeedMultiplier = 0.95 // TODO: improve implementation after multiblock rewrite
//
//		val smelting = furnace.inventory.smelting
//		val fuel = furnace.inventory.fuel
//		val result = furnace.inventory.result
//
//		if (PowerMachines.getPower(sign) == 0 ||
//				smelting == null ||
//				smelting.type != Material.PRISMARINE_CRYSTALS ||
//				fuel == null
//		) {
//			furnace.cookTime = 0
//			event.isCancelled = true
//			return
//		}

//		if (fuel.customItem != CRUDE_FUEL) {
//			furnace.cookTime = 0
//			event.isCancelled = true
//			return
//		}
//
//		if (furnace.cookTime >= 200) {
//			fuel.subtract(1)
//			if (result == null) furnace.inventory.result = REFINED_FUEL.constructItemStack()
//			else result.add(1)
//			PowerMachines.removePower(sign, 300)
//		}
//		furnace.cookTime = 0
//	}
//}