package net.horizonsend.ion.server.features.multiblock.industry

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


object FabricatorMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val name = "fabricator"

	override val signText = createSignText(
		line1 = "&8Fabricator",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val maxPower = 300_000

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

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign,
	) {
		handleRecipe(this, event, furnace, sign)
	}
}
