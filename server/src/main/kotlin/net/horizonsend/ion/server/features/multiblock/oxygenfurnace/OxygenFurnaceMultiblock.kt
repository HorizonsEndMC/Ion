package net.horizonsend.ion.server.features.multiblock.oxygenfurnace

import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.features.customitems.CustomItems.STEEL_INGOT
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

//abstract class OxygenFurnaceMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
//	abstract val speed: Double
//
//	override val name = "oxygenfurnace"
//
//	override val signText = createSignText(
//		line1 = "&2Oxygen",
//		line2 = "&8Furnace",
//		line3 = null,
//		line4 = null
//	)
//
//	override fun MultiblockShape.buildStructure() {
//		z(+0) {
//			y(-1) {
//				x(-1).ironBlock()
//				x(+0).wireInputComputer()
//				x(+1).ironBlock()
// }
//			y(+0) {
//				x(-1).anyGlassPane()
//				x(+0).machineFurnace()
//				x(+1).anyGlassPane()
//			}
//			z(+1)	{
//				y(-1) {
//					x(-1).sponge()
//					x(+0).anyGlass()
//					x(+1).sponge()
//				}
//				y(+0) {
//					x(-1).emeraldBlock()
//					x(+0).anyGlass()
//					x(+1).emeraldBlock()
//				}
//				z(+2) {
//					y(-1) {
//						x(-1).anyStairs()
//						x(+0).aluminumBlock()
//						x(+1).anyStairs()
//					}
//					y(+0) {
//						x(-1).anyStairs()
//						x(+0).aluminumBlock()
//						x(+1).anyStairs()
//					}
//				}
//			}
//		}
//	}

//	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
//		event.isBurning = false
//		event.burnTime = 200
//		event.isCancelled = false
//		furnace.cookSpeedMultiplier = 0.95 // TODO: improve implementation after multiblock rewrite
//
//		val fuel = furnace.inventory.fuel
//		val smelting = furnace.inventory.smelting
//		val result = furnace.inventory.result
//
//		if (PowerMachines.getPower(sign) == 0 ||
//			smelting == null ||
//			smelting.type != Material.PRISMARINE_CRYSTALS ||
//			fuel == null
//			) {
//			furnace.cookTime = 0
//			event.isCancelled = true
//			return
//		}

//		if (fuel.type != Material.IRON_INGOT ||
//			fuel.customItem != null) {
//			furnace.cookTime = 0
//			event.isCancelled = true
//			return
//		}

		// Produce new item if it is not the first burn event
//		if (furnace.cookTime >= 200) {
//			if (result == null) furnace.inventory.result = STEEL_INGOT.constructItemStack()
//			else result.add(1)
//			furnace.inventory.fuel?.subtract(1)
//			PowerMachines.removePower(sign, 250)
//		}
//		furnace.cookTime = 0
//	}
//}
