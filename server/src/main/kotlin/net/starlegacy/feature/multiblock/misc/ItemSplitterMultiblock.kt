package net.starlegacy.feature.multiblock.misc

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.miscellaneous.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.InteractableMultiblock
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.util.getFacing
import net.starlegacy.util.getStateIfLoaded
import net.starlegacy.util.rightFace
import org.bukkit.block.Container
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object ItemSplitterMultiblock : Multiblock(), FurnaceMultiblock, InteractableMultiblock {
	override val name: String = "splitter"

	val BLACKLIST = text("BLACKLIST", NamedTextColor.BLACK, TextDecoration.BOLD)
	val WHITELIST = text("WHITELIST", NamedTextColor.WHITE, TextDecoration.BOLD)

	override val signText: Array<Component?> = arrayOf(
		text("Item Splitter", NamedTextColor.GOLD),
		null,
		null,
		BLACKLIST
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+1) {
				x(0).anyPipedInventory()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).aluminumBlock()
				x(0).furnace()
				x(+1).aluminumBlock()
				x(+2).anyStairs()
			}
		}
		z(+1) {
			y(+1) {
				x(-2).anyPipedInventory()
				x(-1).anyGlass()
				x(0).anyWall()
				x(+1).anyGlass()
				x(+2).anyPipedInventory()
			}
			y(+0) {
				x(-2).craftingTable()
				x(-1).endRod()
				x(0).redstoneBlock()
				x(+1).endRod()
				x(+2).craftingTable()
			}
		}
		z(+2) {
			y(+1) {
				x(0).anyPipedInventory()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).aluminumBlock()
				x(0).aluminumBlock()
				x(+1).aluminumBlock()
				x(+2).anyStairs()
			}
		}
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (isBlacklist(sign)) {
			player.success("Switched sorter to whitelist!")
			sign.line(3, WHITELIST)
		} else {
			player.success("Switched sorter to blacklist!")
			sign.line(3, BLACKLIST)
		}

		sign.update()
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		val filter = getBlacklist(sign)

		val isBlacklist = isBlacklist(sign)

		val inputInventory = getStorage(sign, inputInventory) ?: return
		val filteredInventory = getStorage(sign, filteredInventory) ?: return
		val remainderInventory = getStorage(sign, remainderInventory) ?: return

		if (isBlacklist) {
			doFilter(inputInventory, filteredInventory, remainderInventory) { it?.filterContains(filter) == false }
		} else {
			doFilter(inputInventory, filteredInventory, remainderInventory) { it?.filterContains(filter) == true }
		}
	}

	/** If the lambda returns true, it is put into the filtered inventory, if possible **/
	private fun doFilter(takeFrom: Container, filtered: Container, remainder: Container, filter: (ItemStack?) -> Boolean) {
		val sourceInventory = takeFrom.inventory
		val destinationInventory = filtered.inventory
		val remainderInventory = remainder.inventory

		for ((index, item: ItemStack?) in sourceInventory.withIndex()) {
			if (!filter(item)) {
				tryTransfer(index, sourceInventory, remainderInventory)
				continue
			}

			tryTransfer(index, sourceInventory, destinationInventory)
		}
	}

	private fun tryTransfer(index: Int, sourceInventory: Inventory, destination: Inventory) {
		val item = sourceInventory.contents[index] ?: return

		val result: Int = destination.addItem(item).values.firstOrNull()?.amount ?: 0

		if (result == 0) {
			// no items remaining
			sourceInventory.setItem(index, null)
		} else {
			// if the amount is different, update the item in the original slot
			if (result != item.amount) {
				sourceInventory.setItem(index, item.clone().apply { amount = result })
			}
		}
	}

	private fun ItemStack.filterContains(filter: Collection<ItemStack>): Boolean = filter.any { this.isSimilar(it) }

	private fun getStorage(sign: Sign, offset: Vec3i): Container? {
		val (x, y, z) = offset
		val facing = sign.getFacing()
		val right = facing.rightFace

		val absoluteOffset = Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = y,
			z = (right.modZ * x) + (facing.modZ * z)
		)

		val absolute = absoluteOffset + Vec3i(sign.location)

		val (absoluteX, absoluteY, absoluteZ) = absolute

		return getStateIfLoaded(sign.world, absoluteX, absoluteY, absoluteZ) as? Container
	}

	private fun getBlacklist(sign: Sign): Collection<ItemStack> {
		val items = getStorage(sign, filterInventory)?.inventory ?: return listOf()

		return items.contents.mapNotNull { it }
	}

	private val inputInventory: Vec3i = Vec3i(0, 1, -1)

	private val filterInventory: Vec3i = Vec3i(0, 1, -3)

	private val remainderInventory: Vec3i = Vec3i(-2, 1, -2)

	private val filteredInventory: Vec3i = Vec3i(2, 1, -2)

	private fun isBlacklist(sign: Sign): Boolean {
		return sign.line(3) == BLACKLIST
	}
}
