package net.horizonsend.ion.server.features.multiblock.fabricator

import net.horizonsend.ion.server.features.customitems.CustomItems.FABRICATED_ASSEMBLY
import net.horizonsend.ion.server.features.customitems.CustomItems.FUEL_CELL
import net.horizonsend.ion.server.features.customitems.CustomItems.FUEL_ROD_CORE
import net.horizonsend.ion.server.features.customitems.CustomItems.REACTIVE_ASSEMBLY
import net.horizonsend.ion.server.features.customitems.CustomItems.REINFORCED_FRAME
import net.horizonsend.ion.server.features.customitems.CustomItems.STEEL_ASSEMBLY
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
			y(+0) {
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
			line1 = "&8Fabricator",
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
		event.burnTime = 200
		event.isCancelled = false
		furnace.cookSpeedMultiplier = 0.00138888888 // TODO: improve implementation after multiblock rewrite

		val smelting = furnace.inventory.smelting
		val fuel = furnace.inventory.fuel
		val result = furnace.inventory.result

		if (PowerMachines.getPower(sign) <= 100000 ||
				smelting == null ||
				smelting.type != Material.PRISMARINE_CRYSTALS ||
				fuel == null
		) {
			furnace.cookTime = 0
			event.isCancelled = true
			return
		}

		if (furnace.cookTime >= 200) {
			when (fuel.customItem) {
				FUEL_ROD_CORE -> {
					event.isCancelled = false
					if (result == null) furnace.inventory.result = FUEL_CELL.constructItemStack()
					else if (result.customItem == FUEL_CELL) result.add(1)
					else {
						furnace.cookTime = 0
						event.isCancelled = true
						return
					}
					fuel.subtract(1)
					PowerMachines.removePower(sign, 100000)
				}

				REACTIVE_ASSEMBLY -> {
					event.isCancelled = false
					if (result == null) furnace.inventory.result = FABRICATED_ASSEMBLY.constructItemStack()
					else if (result.customItem == FABRICATED_ASSEMBLY) result.add(1)
					else {
						furnace.cookTime = 0
						event.isCancelled = true
						return
					}
					fuel.subtract(1)
					PowerMachines.removePower(sign, 300)
				}

				STEEL_ASSEMBLY -> {
					event.isCancelled = false
					if (result == null) furnace.inventory.result = REINFORCED_FRAME.constructItemStack()
					else if (result.customItem == REINFORCED_FRAME) result.add(1)
					else {
						furnace.cookTime = 0
						event.isCancelled = true
						return
					}
					fuel.subtract(1)
					PowerMachines.removePower(sign, 300)
				}

				else -> {
					furnace.cookTime = 0
					event.isCancelled = true
					return
				}
			}
		}
		furnace.cookTime = 0
	}
}
