package net.starlegacy.feature.multiblock.ammoreloader

import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

abstract class AmmoReloaderMultiblock(val tierText: String) : PowerStoringMultiblock(), FurnaceMultiblock {
	protected abstract val tierMaterial: Material

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).type(tierMaterial)
				x(+0).wireInputComputer()
				x(+1).type(tierMaterial)
			}

			y(0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).aluminumBlock()
				x(+0).lapisBlock()
				x(+1).aluminumBlock()
			}

			y(+0) {
				x(-1).aluminumBlock()
				x(+0).anyGlass()
				x(+1).aluminumBlock()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).type(tierMaterial)
				x(+0).anyPipedInventory()
				x(+1).type(tierMaterial)
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+1).anyGlassPane()
			}
		}
	}

	override val name = "reloader"

	override val signText = createSignText(
		line1 = "Ammo",
		line2 = "Reloader",
		line3 = null,
		line4 = tierText
	)

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		TODO("Not yet implemented")
	}
}
