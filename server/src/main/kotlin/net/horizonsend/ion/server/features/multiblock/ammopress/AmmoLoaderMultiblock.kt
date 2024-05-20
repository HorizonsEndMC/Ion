package net.horizonsend.ion.server.features.multiblock.ammopress

import net.horizonsend.ion.server.features.customitems.CustomItems.LOADED_TURRET_SHELL
import net.horizonsend.ion.server.features.customitems.CustomItems.UNLOADED_TURRET_SHELL
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
import org.bukkit.inventory.ItemStack

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
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
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
		event.isBurning = false
		event.burnTime = 200
		event.isCancelled = false
		furnace.cookSpeedMultiplier = 0.95 // TODO: improve implementation after multiblock rewrite

		val smelting = furnace.inventory.smelting
		val fuel = furnace.inventory.fuel
		val result = furnace.inventory.result

		if (PowerMachines.getPower(sign) == 0 ||
				smelting == null ||
				smelting.type != Material.PRISMARINE_CRYSTALS ||
				fuel == null ||
				fuel.type != Material.PRISMARINE_CRYSTALS
		) {
			furnace.cookTime = 0
			event.isCancelled = true
			return
		}
		event.isCancelled = false

		// Process if it cooked long enough (avoids the item from processing immediately)

		val direction = sign.getFacing().oppositeFace
		val state = sign.block.getRelative(direction, 7).getState(false)
				as? InventoryHolder ?: return
		val inventory = state.inventory
		if (!state.inventory.containsAtLeast(
				UNLOADED_TURRET_SHELL.constructItemStack(),
				1
			) || !state.inventory.containsAtLeast(ItemStack(Material.GOLD_NUGGET), 1)
		) {
			furnace.cookTime = 0
			event.isCancelled = true
			return
		}

		// Produce new item if it is not the first burn event
		if (furnace.cookTime >= 200) {
			if (result == null) {
				furnace.inventory.result = LOADED_TURRET_SHELL.constructItemStack()
			} else result.add(1)
			inventory.removeItemAnySlot(UNLOADED_TURRET_SHELL.constructItemStack())
			inventory.removeItemAnySlot(ItemStack(Material.GOLD_NUGGET))
			PowerMachines.removePower(sign, 150)
		}
		furnace.cookTime = 0
	}
}
