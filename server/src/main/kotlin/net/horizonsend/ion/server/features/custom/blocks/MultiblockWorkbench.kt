package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.misc.PackagedMultiblock
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.interactable.InteractableGUI
import net.horizonsend.ion.server.features.multiblock.MultiblockRegistration
import net.horizonsend.ion.server.features.multiblock.shape.BlockRequirement
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import java.util.concurrent.TimeUnit

object MultiblockWorkbench : InteractableCustomBlock(
	identifier = "MULTIBLOCK_WORKBENCH",
	blockData = CustomBlocks.mushroomBlockData(setOf(BlockFace.NORTH, BlockFace.DOWN, BlockFace.EAST)),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItems::MULTIBLOCK_WORKBENCH, 1)
	)
) {
	private val cooldown = PerPlayerCooldown(5L, TimeUnit.MILLISECONDS)

	val multiblocks = MultiblockRegistration.getAllMultiblocks().toList()
	private var multiblockIndex = 0
	private val currentMultiblock get() = multiblocks[multiblockIndex]

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		val player = event.player

		cooldown.tryExec(player) { openMenu(player, block.location.toCenterLocation()) }
	}

	private fun openMenu(player: Player, location: Location) {
		val inv = MultiblockWorkbenchMenu(player, location)
		InteractableGUI.setInventory(player.uniqueId, inv)
		inv.open()
	}

	class MultiblockWorkbenchMenu(viewer: Player, val location: Location): InteractableGUI(viewer) {
		override val inventorySize = 36
		override val internalInventory: Inventory = IonServer.server.createInventory(this, inventorySize)

		override fun setup(view: InventoryView) {
			addGuiButton(18, ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
				it.setCustomModelData(GuiItem.LEFT.customModelData)
				it.displayName(text("Previous Multiblock").itemName)
			}) {
				multiblockIndex = (multiblockIndex - 1).coerceAtLeast(0)
				updateMultiblock(it.view)
			}

			addGuiButton(20, ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
				it.setCustomModelData(GuiItem.RIGHT.customModelData)
				it.displayName(text("Next Multiblock").itemName)
			}) {
				multiblockIndex = (multiblockIndex + 1).coerceAtMost(multiblocks.lastIndex)
				updateMultiblock(it.view)
			}

			addGuiButton(28, ItemStack(Material.BARRIER).updateMeta {
				it.setCustomModelData(GuiItem.RIGHT.customModelData)
				it.displayName(text("Missing Materials!", RED).itemName)
			}) {
				tryPack()
				updateConfirmationButton()
			}

			lockedSlots.addAll(setOf(0..11, 27..29).flatten())
			lockedSlots.add(18)
			lockedSlots.add(20)

			view.setTitle(getGuiText(currentMultiblock.getDisplayName()))
		}

		private fun getGuiText(secondLine: Component): Component = GuiText("Multiblock Workbench")
			.setSlotOverlay(
				"# # # # # # # # #",
				"# # # . . . . . .",
				"# . # . . . . . .",
				"# # # . . . . . ."
			)
			.add(secondLine, line = 0)
			.build()

		private fun setSecondLine(view: InventoryView, secondLine: Component) {
			view.setTitle(getGuiText(secondLine))
		}

		private fun updateMultiblock(view: InventoryView) {
			setSecondLine(view, currentMultiblock.getDisplayName())
			updateConfirmationButton()
		}

		private val editSlots = setOf(12..17, 21..26, 30..35).flatten()

		private fun getConsumableItems(): List<ItemStack> {
			return internalInventory.contents.withIndex()
				.filter { editSlots.contains(it.index) }
				.mapNotNull { it.value }
		}

		private fun checkRequirements(): List<BlockRequirement> {
			val items = getConsumableItems().mapTo(mutableListOf()) { it.clone() }

			val itemRequirements = currentMultiblock.shape.getRequirementMap(BlockFace.NORTH).map { it.value }
			val missing = mutableListOf<BlockRequirement>()

			for (blockRequirement in itemRequirements) {
				val requirement = blockRequirement.itemRequirement
				if (items.any { requirement.itemCheck(it) && requirement.consume(it) }) continue
				missing.add(blockRequirement)
			}

			return missing
		}

		fun packageTo(destination: Inventory) {
			val items = getConsumableItems()
			val itemRequirements = currentMultiblock.shape.getRequirementMap(BlockFace.NORTH).map { it.value }
			val missing = mutableListOf<BlockRequirement>()

			for (blockRequirement in itemRequirements) {
				val requirement = blockRequirement.itemRequirement
				val success = items.firstOrNull { requirement.itemCheck(it) && requirement.consume(it) }

				if (success == null) {
					missing.add(blockRequirement)
					continue
				}

				val amount = requirement.amountConsumed(success)
				LegacyItemUtils.addToInventory(destination, success.asQuantity(amount))
			}
		}

		private fun createPackagedItem(): ItemStack {
			val base = PackagedMultiblock.createFor(currentMultiblock)
			return base.updateMeta {
				it as BlockStateMeta

				val newState = Material.CHEST.createBlockData().createBlockState() as Chest
				packageTo(newState.inventory)

				it.blockState = newState
			}
		}

		private var ready: Boolean = false

		private fun updateConfirmationButton() = Tasks.sync {
			val item = inventory.contents[28] ?: return@sync
			val missing = checkRequirements()

			if (missing.isNotEmpty()) {
				item.type = Material.BARRIER
				item.updateMeta {
					it.displayName(text("Missing Materials!", RED).itemName)
					it.lore(missing.groupBy { it.alias }.map { text("${it.key.replaceFirstChar { char -> char.uppercase() }}: ${it.value.size}").itemName })
				}

				ready = false
				return@sync
			}

			item.type = Material.WARPED_FUNGUS_ON_A_STICK
			item.updateMeta {
				it.lore(listOf())
				it.displayName(text("Packaged multiblock ready!", GREEN).itemName)
				it.setCustomModelData(114)
			}

			ready = true
		}

		private fun tryPack() {
			if (!ready) return
			val packagedItem = createPackagedItem()

			if (internalInventory.getItem(19)?.isSimilar(packagedItem) == true) { internalInventory.getItem(19)?.let { it.amount++ } } else {
				internalInventory.setItem(19, packagedItem)
			}

			updateConfirmationButton()
		}

		override fun itemChanged(changedSlot: Int, changedItem: ItemStack) {
			updateConfirmationButton()
		}

		override fun canRemove(slot: Int, player: Player): Boolean { return true }
		override fun canAdd(itemStack: ItemStack, slot: Int, player: Player): Boolean { return true }

		override fun handleClose(event: InventoryCloseEvent) {
			for ((slot, content) in inventory.contents.withIndex()) {
				if (noDropSlots.contains(slot)) continue
				viewer.world.dropItemNaturally(location, content ?: continue)
			}
		}
	}
}
