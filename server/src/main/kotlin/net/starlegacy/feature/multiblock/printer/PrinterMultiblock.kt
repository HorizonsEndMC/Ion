package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.LegacyItemUtils
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

abstract class PrinterMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val name: String = "printer"
	override val maxPower: Int = 50_000
	override val inputComputerOffset = Vec3i(0, -1, 0)
	abstract fun getOutput(product: Material): ItemStack

	protected abstract fun LegacyMultiblockShape.RequirementBuilder.printerCoreBlock()
	protected abstract fun LegacyMultiblockShape.RequirementBuilder.printerMachineryBlock()
	protected abstract fun LegacyMultiblockShape.RequirementBuilder.printerProductBlock()

	override fun onTransformSign(player: Player, sign: Sign) {
		super<PowerStoringMultiblock>.onTransformSign(player, sign)
	}

	override fun LegacyMultiblockShape.buildStructure() {
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
				x(-1).copperBlock()
				x(+0).printerMachineryBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).copperBlock()
				x(+0).printerCoreBlock()
				x(+1).anyGlass()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).printerProductBlock()
				x(+1).anyGlass()
			}
		}

		z(+3) {
			y(-1) {
				x(-1).copperBlock()
				x(+0).printerMachineryBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
			}
		}

		z(+4) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).hopper()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyPipedInventory()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		event.isCancelled = true
		val smelting = furnace.inventory.smelting
		val fuel = furnace.inventory.fuel

		if (PowerMachines.getPower(sign) == 0 ||
			smelting == null ||
			smelting.type != Material.PRISMARINE_CRYSTALS ||
			fuel == null ||
			fuel.type != Material.COBBLESTONE
		) {
			return
		}

		event.isBurning = false
		event.burnTime = 100
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false

		val direction = sign.getFacing().oppositeFace

		val state = sign.block.getRelative(direction, 5).getState(false)
			as? InventoryHolder ?: return

		val product = sign.block.getRelative(sign.getFacing().oppositeFace, 3).type
		val output = getOutput(product)

		val inventory = state.inventory
		if (!LegacyItemUtils.canFit(inventory, output)) {
			return
		}

		LegacyItemUtils.addToInventory(inventory, output)
		fuel.amount = fuel.amount - 1
		PowerMachines.removePower(sign, 250)
	}
}
