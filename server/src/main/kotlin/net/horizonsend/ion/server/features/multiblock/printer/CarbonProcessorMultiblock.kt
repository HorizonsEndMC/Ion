package net.horizonsend.ion.server.features.multiblock.printer

import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isConcretePowder
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

object CarbonProcessorMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower: Int = 30000
	override val name = "processor"

	override val signText = createSignText(
		line1 = "&3Carbon",
		line2 = "&7Processor",
		line3 = null,
		line4 = "&7:[''']:"
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
				x(+0).goldBlock()
				x(+1).sponge()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).stainedGlass()
				x(+1).anyGlass()
			}
		}

		z(+2) {
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

	fun getOutputBlock(sign: Block): Block {
		return sign.getRelative((sign.getState(false) as Sign).getFacing().oppositeFace, 3)
	}

	fun getOutput(sign: Block): ItemStack {
		val direction: BlockFace = (sign.getState(false) as Sign).getFacing().oppositeFace
		val typeName = sign.getRelative(direction, 2).type.name.replace("STAINED_GLASS", "CONCRETE")
		val type = Material.getMaterial(typeName) ?: error("No material $typeName")
		return ItemStack(type, 1)
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
			!fuel.type.isConcretePowder
		) {
			return
		}
		val inventory = (getOutputBlock(sign.block).getState(false) as InventoryHolder).inventory
		val output = getOutput(sign.block)
		if (!LegacyItemUtils.canFit(inventory, output)) {
			return
		}
		LegacyItemUtils.addToInventory(inventory, output)
		PowerMachines.removePower(sign, 100)
		fuel.amount = fuel.amount - 1
		event.isBurning = false
		event.burnTime = 50
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false
	}
}
