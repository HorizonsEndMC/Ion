package net.starlegacy.feature.multiblock.ammopress

import net.horizonsend.ion.server.features.blasters.objects.AmmunitionHoldingItem
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.getFacing
import org.bukkit.Material
import org.bukkit.Material.matchMaterial
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.lang.Integer.min

abstract class AmmoPressMultiblock : PowerStoringMultiblock(), FurnaceMultiblock {

	override fun LegacyMultiblockShape.buildStructure() {
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

		// deposit blaster/magazine into output if full
		if ((fuelCustomItem as AmmunitionHoldingItem).getAmmunition(fuel) == fuelCustomItem.getMaximumAmmunition()) {
			val result = furnace.inventory.result
			if (result != null && result.type != Material.AIR) return
			furnace.inventory.result = event.fuel
			furnace.inventory.fuel = null
			return
		}

		// refill item check
		val direction = sign.getFacing().oppositeFace
		val state = sign.block.getRelative(direction, 7).getState(false)
			as? InventoryHolder ?: return
		val inventory = state.inventory
		val typeRefill = matchMaterial(fuelCustomItem.getTypeRefill()) ?: return
		if (!inventory.containsAtLeast(ItemStack(typeRefill), 1)) return

		event.isBurning = false
		event.burnTime = 200
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false

		val ammoToSet = min(
			fuelCustomItem.getMaximumAmmunition() - fuelCustomItem.getAmmunition(fuel),
			fuelCustomItem.getAmmoPerRefill()
		)
		fuelCustomItem.setAmmunition(fuel, furnace.inventory, fuelCustomItem.getAmmunition(fuel) + ammoToSet)
		inventory.removeItemAnySlot(ItemStack(typeRefill))
		PowerMachines.removePower(sign, 250)
	}
}
