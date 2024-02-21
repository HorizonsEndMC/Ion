package net.horizonsend.ion.server.features.multiblock.oxygenfurnace

import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.features.customitems.CustomItems.STEEL_INGOT
import net.horizonsend.ion.server.features.customblocks.CustomBlock
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.GAS_CANISTER_OXYGEN
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Item
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.ItemStack

abstract class OxygenFurnaceMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	abstract val speed: Double

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 200
		val inventory = furnace.inventory
		val result = inventory.result
		val fuel = inventory.fuel
		val smelting = inventory.smelting


		if (smelting != null && smelting.type != Material.PRISMARINE_CRYSTALS) {
			return
		}

		if (PowerMachines.getPower(sign) < 250) return
		if (fuel?.type == Material.IRON_INGOT) {
			if (result == null) {
				furnace.inventory.result = STEEL_INGOT.constructItemStack()
			}
			else furnace.inventory.result?.add(1)?.customItem?.constructItemStack()
			furnace.inventory.fuel?.subtract(1)
			PowerMachines.removePower(sign, 250)
		}

		else return
	}

	override val name = "oxygenfurnace"

	override val signText = createSignText(
			line1 = "&2Oxygen",
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
		z(+1)	{
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
		z(+2) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).aluminumBlock()
				x(+1).anyStairs()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).aluminumBlock()
				x(+1).anyStairs()
			}
		}
		}
	}
}
}
