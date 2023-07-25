package net.horizonsend.ion.server.features.multiblock.powerfurnace

import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

abstract class PowerFurnaceMultiblock(tierText: String) : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val name = "powerfurnace"

	protected abstract val burnTime: Int
	protected abstract val tierMaterial: Material

	override val signText = createSignText(
		line1 = "&6Power",
		line2 = "&4Furnace",
		line3 = null,
		line4 = tierText
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
				x(+1).anyGlassPane()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).type(tierMaterial)
				x(+0).anyGlass()
				x(+1).type(tierMaterial)
			}

			y(+0) {
				x(-1).type(tierMaterial)
				x(+0).anyGlass()
				x(+1).type(tierMaterial)
			}
		}
	}

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		if (PowerMachines.getPower(sign) == 0) {
			event.isCancelled = true
			return
		}

		val fuel = furnace.inventory.fuel ?: return
		furnace.cookTime = 100.toShort()
		event.isCancelled = false
		event.isBurning = false
		event.burnTime = burnTime

		PowerMachines.removePower(sign, 30)
	}
}
