package net.horizonsend.ion.server.legacy.multiblocks

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.getFacing
import net.starlegacy.util.leftFace
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

object DisposalMultiblock : PowerStoringMultiblock(), FurnaceMultiblock {
	override val name = "incinerator"

	override var signText: Array<Component?> = arrayOf(
		Component.text("Incinerator").color(NamedTextColor.RED),
		null,
		null,
		null
	)

	override val maxPower: Int = 150_000

	private const val powerConsumed = 0.5

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyPipedInventory()
				x(+0).wireInputComputer()
				x(+1).ironBlock()
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
					x(-1).stainedTerracotta()
					x(+0).sponge()
					x(+1).stainedTerracotta()
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

		// Clear while checking for power
		for (i in 0 until inventory.size) {
			val size = (inventory.getItem(i) ?: continue).amount
			if ((size * powerConsumed) + (amountToClear * 3) >= power) continue
			amountToClear += size
			inventory.clear(i)
		}

		PowerMachines.setPower(sign, power - 3 * amountToClear)
		furnace.cookTime = 20.toShort()

		event.isCancelled = true
		event.isBurning = false
		event.burnTime = 20
	}
}
