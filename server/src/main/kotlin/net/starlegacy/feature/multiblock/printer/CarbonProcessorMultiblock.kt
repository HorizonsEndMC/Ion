package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.LegacyItemUtils
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.isConcretePowder
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

object CarbonProcessorMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower: Int = 30000
	override val name = "processor"

	override val inputComputerOffset = Vec3i(0, -1, 0)

	override val signText = createSignText(
		line1 = "&3Carbon",
		line2 = "&7Processor",
		line3 = null,
		line4 = "&7:[''']:"
	)

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
