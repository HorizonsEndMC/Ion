package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import kotlin.math.roundToInt

abstract class AbstractDisposalMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val name = "incinerator"

	override var signText: Array<Component?> = arrayOf(
		Component.text("Incinerator").color(NamedTextColor.RED),
		null,
		null,
		null
	)

	companion object {
		private const val powerConsumed = 0.5
	}

	override val maxPower: Int = 150_000
	abstract val mirrored: Boolean

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				if (!mirrored) x(-1).anyPipedInventory() else x(-1).ironBlock()
				x(+0).wireInputComputer()
				if (!mirrored) x(+1).ironBlock() else x(+1).anyPipedInventory()
			}
			y(0) {
				x(-1).anyStairs()
				x(+0).machineFurnace()
				x(+1).anyStairs()
			}
			y(+1) {
				x(0).anySlab()
			}
			z(+1) {
				y(-1) {
					x(-1).terracotta()
					x(+0).sponge()
					x(+1).terracotta()
				}
				y(+0) {
					x(-1).anyGlassPane()
					x(+0).type(Material.MAGMA_BLOCK)
					x(+1).anyGlassPane()
				}
				y(+1) {
					x(-1).anySlab()
					x(0).anySlab()
					x(+1).anySlab()
				}
			}
			z(+2) {
				y(-1) {
					x(-1).anyStairs()
					x(+0).redstoneBlock()
					x(+1).anyStairs()
				}
				y(+0) {
					x(-1).goldBlock()
					x(+0).anyGlassPane()
					x(+1).goldBlock()
				}
				y(+1) {
					x(0).anySlab()
				}
			}
		}
	}

	private fun getOutput(sign: Sign): Inventory {
		val direction = sign.getFacing().oppositeFace
		return (
			sign.block.getRelative(direction)
				.getRelative(direction.leftFace)
				.getRelative(BlockFace.DOWN)
				.getState(false) as InventoryHolder
			)
			.inventory
	}

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		event.isBurning = false
		event.burnTime = 0
		val inventory = getOutput(sign)
		val power = PowerMachines.getPower(sign)
		if (power == 0) return

		var amountToClear = 0

		if (inventory.isEmpty) {
			event.burnTime = 200

			return
		}

		// Clear while checking for power
		for (i in 0 until inventory.size) {
			val size = (inventory.getItem(i) ?: continue).amount
			if ((size * powerConsumed) + (amountToClear * 3) >= power) continue
			amountToClear += size
			inventory.clear(i)
		}

		PowerMachines.setPower(sign, power - (powerConsumed * amountToClear).roundToInt())
		furnace.cookTime = 20.toShort()

		event.isCancelled = true
		event.isBurning = false
		event.burnTime = 20
	}
}

object DisposalMultiblock : AbstractDisposalMultiblock() {
	override val mirrored = false
}

object DisposalMultiblockMirrored : AbstractDisposalMultiblock() {
	override val mirrored = true
}