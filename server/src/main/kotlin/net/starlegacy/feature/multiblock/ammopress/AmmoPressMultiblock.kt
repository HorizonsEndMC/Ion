package net.starlegacy.feature.multiblock.ammopress

import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

abstract class AmmoPressMultiblock() : PowerStoringMultiblock(), FurnaceMultiblock {

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anySlab()
				x(+0).wireInputComputer()
				x(+1).anySlab()
			}

			y(0) {
				x(-1).anySlab()
				x(+0).machineFurnace()
				x(+1).anySlab()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).type(Material.GRINDSTONE)
				x(+1).anyStairs()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).copperBlock()
				x(+0).sponge()
				x(+1).copperBlock()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
			}
		}

		z(+3) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).aluminumBlock()
				x(+1).anyGlassPane()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).type(Material.ANVIL)
				x(+1).anyGlassPane()
			}
		}

		z(+4) {
			y(-1) {
				x(-1).copperBlock()
				x(+0).sponge()
				x(+1).copperBlock()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
			}
		}

		z(+5) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).type(Material.GRINDSTONE)
				x(+1).anyStairs()
			}
		}
		z(+6) {
			y(-1) {
				x(-1).anySlab()
				x(+0).anyGlass()
				x(+1).anySlab()
			}

			y(0) {
				x(-1).anySlab()
				x(+0).anyPipedInventory()
				x(+1).anySlab()
			}
		}
	}

	override val name = "ammopress"

	override val signText = createSignText(
		line1 = "&6Ammo",
		line2 = "&8Press",
		line3 = null,
		line4 = null
	)

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		TODO("Not yet implemented")
	}
}
