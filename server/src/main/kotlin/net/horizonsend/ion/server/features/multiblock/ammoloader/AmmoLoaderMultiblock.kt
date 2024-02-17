package net.horizonsend.ion.server.features.multiblock.ammoloader

import net.horizonsend.ion.server.features.customitems.CustomItems.LOADED_TURRET_SHELL
import net.horizonsend.ion.server.features.customitems.CustomItems.UNLOADED_TURRET_SHELL
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.InventoryHolder


abstract class AmmoLoaderMultiblock	: Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
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
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anySlab()
				x(+0).type(Material.GRINDSTONE)
				x(+1).anySlab()
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
				x(-1).anySlab()
				x(+0).type(Material.GRINDSTONE)
				x(+1).anySlab()
			}
		}
		z(+6) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}

			y(0) {
				x(+0).anyPipedInventory()
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

	override fun onFurnaceTick(
			event: FurnaceBurnEvent,
			furnace: Furnace,
			sign: Sign
	) {
		val smelting = furnace.inventory.smelting
		val fuel = furnace.inventory.fuel
		val fuelCustomItem = fuel?.customItem

		if (PowerMachines.getPower(sign) == 0 ||
				smelting == null ||
				smelting.type != Material.PRISMARINE_CRYSTALS ||
				fuel == null ||
				fuelCustomItem == null
		) {
			return
		}


		val direction = sign.getFacing().oppositeFace
		val state = sign.block.getRelative(direction, 7).getState(false)
				as? InventoryHolder ?: return
		val inventory = state.inventory
		if (fuelCustomItem != UNLOADED_TURRET_SHELL) return
		if (!inventory.contains(org.bukkit.Material.GOLD_NUGGET )) return

		event.isBurning = false
		event.burnTime = 200
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false

		furnace.inventory.removeItemAnySlot(UNLOADED_TURRET_SHELL.constructItemStack())
		furnace.inventory.addItem(LOADED_TURRET_SHELL.constructItemStack())
	}
}
