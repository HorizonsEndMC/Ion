package net.horizonsend.ion.server.features.multiblock.industry

import net.horizonsend.ion.server.features.customitems.CustomItems.ENRICHED_URANIUM
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


object CentrifugeMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower: Int = 300_000

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).steelBlock()
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
				x(+1).anyGlassPane()
				x(+2).steelBlock()
			}
			y(+0) {
				x(-2).steelBlock()
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
				x(+2).steelBlock()
			}
		}
		z(+1) {
			y(-1) {
				x(-2).anyGlassPane()
				x(-1).copperBlock()
				x(+0).endRod()
				x(+1).copperBlock()
				x(+2).anyGlassPane()
			}
			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
				x(+2).anyGlassPane()
			}
		}
		z(+2) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).copperBlock()
				x(+0).sponge()
				x(+1).copperBlock()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).sculkCatalyst()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}
		z(+3) {
			y(-1) {
				x(-2).anyGlassPane()
				x(-1).copperBlock()
				x(+0).endRod()
				x(+1).copperBlock()
				x(+2).anyGlassPane()
			}
			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
				x(+2).anyGlassPane()
			}
		}
		z(+4) {
			y(-1) {
				x(-2).steelBlock()
				x(-1).anyGlassPane()
				x(+0).sponge()
				x(+1).anyGlassPane()
				x(+2).steelBlock()
			}
			y(+0) {
				x(-2).steelBlock()
				x(-1).anyGlassPane()
				x(+0).ironBlock()
				x(+1).anyGlassPane()
				x(+2).steelBlock()
			}
		}
		}
		}

	override val name = "centrifuge"

	override val signText = createSignText(
			line1 = "&6Centrifuge",
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
		furnace.cookSpeedMultiplier = 0.16666666666 // TODO: improve implementation after multiblock rewrite

		val smelting = furnace.inventory.smelting
		val fuel = furnace.inventory.fuel
		val result = furnace.inventory.result

		if (PowerMachines.getPower(sign) <= 100 ||
				smelting == null ||
				smelting.type != Material.PRISMARINE_CRYSTALS ||
				fuel == null
		) {
			furnace.cookTime = 0
			event.isCancelled = true
			return
		}

		if (fuel.customItem != URANIUM) {
			furnace.cookTime = 0
			event.isCancelled = true
			return
		}

		// Produce new item if it is not the first burn event
		if (furnace.cookTime >= 200) {
			fuel.subtract(1)
			if (result == null) furnace.inventory.result = ENRICHED_URANIUM.constructItemStack()
			else result.add(1)
			PowerMachines.removePower(sign, 100)
			event.isCancelled = false
		}
		furnace.cookTime = 0
	}
}
