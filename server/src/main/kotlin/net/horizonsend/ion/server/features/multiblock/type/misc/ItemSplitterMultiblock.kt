package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SPLITTER_DIRECTION
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getStateSafe
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.block.Container
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ItemSplitterMultiblock : Multiblock(), FurnaceMultiblock, InteractableMultiblock {
	override val name: String = "splitter"

	private fun formatText(text: String, vararg params: String) = template(text(text, AQUA), paramColor = YELLOW, useQuotesAroundObjects = false, *params)

	private val RIGHT = formatText("[{0}]   ----->   [{1}]", "-", "+")
	private val LEFT = formatText("[{0}]   <-----   [{1}]", "+", "-")

	override val signText: Array<Component?> = arrayOf(
		text("Item Splitter", NamedTextColor.GOLD),
		null,
		text(".:[Matching items]:;", NamedTextColor.GRAY),
		RIGHT
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
		val pdc = sign.persistentDataContainer

		if (isBlacklist(sign)) {
			player.success("Switched sorter to whitelist!")
			pdc.set(SPLITTER_DIRECTION, PersistentDataType.BOOLEAN, false)
			sign.getSide(Side.FRONT).line(3, LEFT)
		} else {
			player.success("Switched sorter to blacklist!")
			pdc.set(SPLITTER_DIRECTION, PersistentDataType.BOOLEAN, true)
			sign.getSide(Side.FRONT).line(3, RIGHT)
		}

		sign.update()
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		migrate(sign)

		event.isBurning = false
		event.burnTime = 20

		furnace.cookTime = (-1000).toShort()

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
	private fun doFilter(takeFrom: InventoryHolder, filtered: InventoryHolder, remainder: InventoryHolder, filter: (ItemStack?) -> Boolean) {
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

		return getStateSafe(sign.world, absoluteX, absoluteY, absoluteZ) as? Container
	}

	private fun getBlacklist(sign: Sign): Collection<ItemStack> {
		val items = getStorage(sign, filterInventory)?.inventory ?: return listOf()

		return items.storageContents.filterNotNull()
	}

	private val inputInventory: Vec3i = Vec3i(0, 1, -1)

	private val filterInventory: Vec3i = Vec3i(0, 1, -3)

	private val remainderInventory: Vec3i = Vec3i(-2, 1, -2)

	private val filteredInventory: Vec3i = Vec3i(2, 1, -2)

	private fun isBlacklist(sign: Sign): Boolean {
		return sign.persistentDataContainer.getOrDefault(SPLITTER_DIRECTION, PersistentDataType.BOOLEAN, true)
	}

	private val RIGHT_OLD = text("<-----", AQUA)
	private val LEFT_OLD = text("----->", AQUA)
	private val BLACKLIST_OLD = text("BLACKLIST", NamedTextColor.BLACK, TextDecoration.BOLD)
	private val WHITELIST_OLD = text("WHITELIST", NamedTextColor.WHITE, TextDecoration.BOLD)

	private val previousBlacklistText = listOf<Component>(
		BLACKLIST_OLD,
		RIGHT_OLD
	)

	/** Migrates an old splitter to the new PDC format / sign text */
	private fun migrate(sign: Sign) {
		val pdc = sign.persistentDataContainer
		if (pdc.keys.contains(SPLITTER_DIRECTION)) return

		val line3 = sign.getSide(Side.FRONT).line(3)

		if (previousBlacklistText.contains(line3)) {
			pdc.set(SPLITTER_DIRECTION, PersistentDataType.BOOLEAN, true)
			sign.getSide(Side.FRONT).line(3, RIGHT)
			return
		}

		pdc.set(SPLITTER_DIRECTION, PersistentDataType.BOOLEAN, false)
		sign.getSide(Side.FRONT).line(3, LEFT)
	}
}
