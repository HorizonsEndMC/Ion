package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.custom.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.InventoryHolder

object GasFurnaceMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower: Int = 250_000
	override val name = "gasfurnace"

	override val signText = createSignText(
		line1 = "&2Gas",
		line2 = "&8Furnace",
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).wireInputComputer()
				x(+1).ironBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).sponge()
				x(+0).anyGlass()
				x(+1).sponge()
			}
			y(+0) {
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).sponge()
				x(+0).aluminumBlock()
				x(+1).sponge()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).aluminumBlock()
				x(+1).anyStairs()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).craftingTable()
				x(+1).anyStairs()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).anyPipedInventory()
				x(+1).anyStairs()
			}
		}
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		handleRecipe(this, event, furnace, sign)

		if (furnace.inventory.fuel?.customItem != CustomItemRegistry.GAS_CANISTER_EMPTY) return

		val inventoryHolder = furnace.block.getRelative(sign.getFacing().oppositeFace, 3).state as InventoryHolder

		val noFit = inventoryHolder.inventory.addItem(Gasses.EMPTY_CANISTER).values.isNotEmpty()

		if (noFit) return

		furnace.inventory.fuel = null
	}
}
