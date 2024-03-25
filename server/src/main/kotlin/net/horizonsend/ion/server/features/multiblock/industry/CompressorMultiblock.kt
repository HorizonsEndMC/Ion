package net.horizonsend.ion.server.features.multiblock.industry

import net.horizonsend.ion.server.features.customitems.CustomItems.URANIUM_CORE
import net.horizonsend.ion.server.features.customitems.CustomItems.URANIUM_ROD
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


object CompressorMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower = 300_000

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
				x(-1).netheriteBlock()
				x(+0).endRod()
				x(+1).netheriteBlock()
			}
			y(+0) {
				x(-1).ironBlock()
				x(+0).craftingTable()
				x(+1).ironBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).netheriteBlock()
				x(+0).endRod()
				x(+1).netheriteBlock()
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
				x(+0).lodestone()
				x(+1).anyGlass()
			}
		}
		z(+4) {
			y(-1) {
				x(-1).netheriteBlock()
				x(+0).endRod()
				x(+1).netheriteBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).pistonBase()
				x(+1).anyGlassPane()
			}
		}
		z(+5) {
			y(-1) {
				x(-1).netheriteBlock()
				x(+0).endRod()
				x(+1).netheriteBlock()
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


	override val name = "compressor"

	override val signText = createSignText(
			line1 = "&6Compressor",
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
		furnace.cookSpeedMultiplier = 0.00277777777 // TODO: improve implementation after multiblock rewrite

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

		if (fuel.customItem != URANIUM_CORE) {
			furnace.cookTime = 0
			event.isCancelled = true
			return
		}

		if (furnace.cookTime >= 200) {
			fuel.subtract(1)
			if (result == null) furnace.inventory.result = URANIUM_ROD.constructItemStack()
			else result.add(1)
			PowerMachines.removePower(sign, 100000)
		}
		furnace.cookTime = 0
	}
}
