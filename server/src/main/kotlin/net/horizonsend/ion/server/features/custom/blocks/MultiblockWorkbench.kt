package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.ValidatorResult
import net.horizonsend.ion.server.features.gui.interactable.InteractableGUI
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockRegistration
import net.horizonsend.ion.server.features.multiblock.PrePackaged.checkRequirements
import net.horizonsend.ion.server.features.multiblock.PrePackaged.createPackagedItem
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.text.loreName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
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

		private companion object {
			const val SEARCH_BUTTON_SLOT = 10
			const val LEFT_BUTTON_SLOT = 18
			const val RESULT_SLOT = 19
			const val RIGHT_BUTTON_SLOT = 20
			const val CONFIRM_BUTTON_SLOT = 28

			val BACKGROUND_SLOTS = setOf(0..11, 27..29).flatten()
			val INVENTORY_SLOTS = setOf(12..17, 21..26, 30..35).flatten()
		}

		override fun setup(view: InventoryView) {
			lockedSlots.addAll(BACKGROUND_SLOTS)

			addGuiButton(LEFT_BUTTON_SLOT, ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
				it.setCustomModelData(GuiItem.LEFT.customModelData)
				it.displayName(text("Previous Multiblock").itemName)
			}) {
				multiblockIndex = (multiblockIndex - 1).coerceAtLeast(0)
				refreshMultiblock(it.view)
			}

			addGuiButton(RIGHT_BUTTON_SLOT, ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
				it.setCustomModelData(GuiItem.RIGHT.customModelData)
				it.displayName(text("Next Multiblock").itemName)
			}) {
				multiblockIndex = (multiblockIndex + 1).coerceAtMost(multiblocks.lastIndex)
				refreshMultiblock(it.view)
			}

			addGuiButton(SEARCH_BUTTON_SLOT, ItemStack(Material.NAME_TAG).setDisplayNameAndGet(text("Search").itemName)) {
				openSearchMenu(it.playerClicker)
			}

			addGuiButton(CONFIRM_BUTTON_SLOT, ItemStack(Material.BARRIER)) {
				tryPack()
				refreshButtons()
			}

			// Perform full setup of the buttons
			refreshMultiblock(view)
			isSearching = false
		}

		private fun setGuiOverlay(view: InventoryView) {
			val text = GuiText("Multiblock Workbench")
				.setSlotOverlay(
					"# # # # # # # # #",
					"# # # . . . . . .",
					"# . # . . . . . .",
					"# # # . . . . . ."
				)
				.add(currentMultiblock.getDisplayName(), line = 0)
				.build()

			view.setTitle(text)
		}

		private fun refreshMultiblock(view: InventoryView) {
			internalInventory.getItem(LEFT_BUTTON_SLOT)?.updateMeta {
				it.lore(listOf(
					if ((multiblockIndex - 1) >= 0) ofChildren(
						text("Previous Multiblock: ", GRAY), multiblocks[multiblockIndex - 1].getDisplayName()
					).loreName else text("No previous multiblock", RED).loreName
				))
			}
			internalInventory.getItem(RIGHT_BUTTON_SLOT)?.updateMeta {
				it.lore(listOf(
					if ((multiblockIndex + 1) <= multiblocks.lastIndex) ofChildren(
						text("Next Multiblock: ", GRAY), multiblocks[multiblockIndex + 1].getDisplayName()
					).loreName else text("No previous multiblock", RED).loreName
				))
			}

			setGuiOverlay(view)
			refreshButtons()
		}

		private var ready: Boolean = false

		/**
		 * Update the confirmation button to indicate whether the item requirements are fulfilled
		 **/
		private fun refreshButtons() = Tasks.sync {
			val item = inventory.contents[CONFIRM_BUTTON_SLOT] ?: return@sync
			val missing = checkRequirements(getUnlockedItems(), currentMultiblock)

			if (missing.isNotEmpty()) {
				item.type = Material.BARRIER
				val missingLore = missing
					.groupBy { it.alias }
					// Group by the same alias, count the number needed of that alias. Get a result like "Any slab: 3"
					.map { (description , entries) -> text("${description.replaceFirstChar { char -> char.uppercase() }}: ${entries.size}", WHITE).itemName }

				item.updateMeta {
					it.displayName(text("Missing Materials!", RED).itemName)
					it.lore(missingLore)
				}

				ready = false
				return@sync
			}

			item.type = Material.WARPED_FUNGUS_ON_A_STICK
			item.updateMeta {
				it.lore(listOf())
				it.displayName(text("Packaged multiblock ready!", GREEN).itemName)
				it.setCustomModelData(GuiItem.CHECKMARK.customModelData )
			}

			ready = true
		}

		private fun tryPack() {
			if (!ready) return
			if (checkRequirements(getUnlockedItems(), currentMultiblock).isNotEmpty()) return // Just double check I don't want a dupe from a stuck ready state
			val packagedItem = createPackagedItem(getUnlockedItems(), currentMultiblock)

			// Increment or set the item
			val currentItem = internalInventory.getItem(RESULT_SLOT)

			if (currentItem == null) {
				internalInventory.setItem(RESULT_SLOT, packagedItem)
			} else {
				currentItem.amount++
			}

			// Update after items have been consumed
			refreshButtons()
		}

		override fun itemChanged(changedSlot: Int, changedItem: ItemStack) = refreshButtons()
		override fun canRemove(slot: Int, player: Player): Boolean { return true }
		override fun canAdd(itemStack: ItemStack, slot: Int, player: Player): Boolean { return true }

		private var isSearching = false

		override fun handleClose(event: InventoryCloseEvent) {
			if (isSearching) return
			dropItems(location)
		}

		private fun openSearchMenu(player: Player) {
			isSearching = true

			TextInputMenu(
				player,
				text("Search by Multiblock Name"),
				empty(),
				backButtonHandler = {
					this.open()
					isSearching = false
				},
				inputValidator = { input ->
					isSearching = true // Double check to make sure this is set whenever something is typed
					val searchResults = getSearchResults(input)

					if (searchResults.isNotEmpty()) ValidatorResult.ResultsResult(searchResults.map { text(it.javaClass.simpleName, GRAY).itemName })
						else ValidatorResult.FailureResult(text("No multiblocks found!", RED))
				},
				successfulInputHandler = {
					runCatching {
						val multiblock = getSearchResults(it).firstOrNull() ?: return@TextInputMenu
						multiblockIndex = multiblocks.indexOf(multiblock)
						player.closeInventory()
						open()
						refreshButtons()

						isSearching = false
					}
				}
			).open()
		}

		private fun getSearchResults(input: String): List<Multiblock> {
			val detectionNames = MultiblockRegistration.byDetectionName.keys()
				.filter { detectionName -> input.split(' ').all { splitInput -> detectionName.contains(splitInput, ignoreCase = true) } }
				.mapNotNull(MultiblockRegistration::getByStorageName)

			val classNames = MultiblockRegistration.getAllMultiblocks().map { it.javaClass.simpleName }
				.filter { className -> input.split(' ').all { splitInput -> className.contains(splitInput, ignoreCase = true) } }
				.mapNotNull(MultiblockRegistration::getByStorageName)

			return detectionNames
				.plus(classNames)
				.distinct()
				.filter { if (it.requiredPermission != null) viewer.hasPermission(it.requiredPermission!!) else true }
		}
	}
}
