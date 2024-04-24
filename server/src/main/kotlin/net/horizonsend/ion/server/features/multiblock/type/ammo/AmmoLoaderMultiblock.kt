package net.horizonsend.ion.server.features.multiblock.type.ammo

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

object AmmoLoaderMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower = 250_000

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).wireInputComputer()
				x(+1).anyStairs()
			}

			y(0) {
				x(+0).machineFurnace()
			}
		}

		z(+1) {
			y(-1) {
				x(-2).anyWall()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyWall()
			}

			y(+0) {
				x(-1).anySlab()
				x(+0).type(Material.GRINDSTONE)
				x(+1).anySlab()
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
				x(-2).anyStairs()
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
		}

		z(+3) {
			y(-1) {
				x(-2).anyGlassPane()
				x(-1).copperBlock()
				x(+0).aluminumBlock()
				x(+1).copperBlock()
				x(+2).anyGlassPane()
			}

			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyGlass()
				x(+0).type(Material.ANVIL)
				x(+1).anyGlass()
				x(+2).anyGlassPane()
			}
		}

		z(+4) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).copperBlock()
				x(+0).sponge()
				x(+1).copperBlock()
				x(+2).ironBlock()
			}

			y(+0) {
				x(-2).anyStairs()
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
		}

		z(+5) {
			y(-1) {
				x(-2).anyWall()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyWall()
			}

			y(+0) {
				x(-1).anySlab()
				x(+0).type(Material.GRINDSTONE)
				x(+1).anySlab()
			}
		}
		z(+6) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(0) {
				x(+0).ironBlock()
			}
		}
	}

	override val name = "ammoloader"

	override val signText = createSignText(
			line1 = "&6Ammo",
			line2 = "&8Loader",
			line3 = null,
			line4 = null
	)

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		handleRecipe(this, event, furnace, sign)
	}
}
