package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent


object CentrifugeMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
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
				x(-1).anyCopperVariant()
				x(+0).endRod()
				x(+1).anyCopperVariant()
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
				x(-1).anyCopperVariant()
				x(+0).sponge()
				x(+1).anyCopperVariant()
				x(+2).ironBlock()
			}
			y(+0) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).sculkCatalyst()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}
		}
		z(+3) {
			y(-1) {
				x(-2).anyGlassPane()
				x(-1).anyCopperVariant()
				x(+0).endRod()
				x(+1).anyCopperVariant()
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
		sign: Sign,
	) {
		handleRecipe(this, event, furnace, sign)
	}
}
