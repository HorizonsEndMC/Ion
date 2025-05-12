package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.common.utils.text.withShadowColor
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.bazaar.BAZAAR_SHADOW_COLOR
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLACK
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

class ListListingMenu(viewer: Player, backButtonHandler: () -> Unit = {}) : AbstractListingMenu(viewer, backButtonHandler) {
	companion object {
		private const val LISTINGS_PER_PAGE = 4
	}

	override fun buildWindow(): Window {
		val guiItems = generateItemListings()

		val gui = PagedGui.items()
			.setStructure(
				"x . . . f S s g i",
				"# 0 0 0 0 0 0 0 0",
				"# 1 1 1 1 1 1 1 1",
				"# 2 2 2 2 2 2 2 2",
				"# 3 3 3 3 3 3 3 3",
				"< . . . . . . . >",
			)
			.addIngredient('x', backButton)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('f', filterButton)
			.addIngredient('S', searchButton)
			.addIngredient('s', sortButton)
			.addIngredient('g', gridViewButton)
			.addIngredient('i', infoButton)
			.addIngredient('0', backingButton(0))
			.addIngredient('1', backingButton(1))
			.addIngredient('2', backingButton(2))
			.addIngredient('3', backingButton(3))
			.addPageChangeHandler { _, new ->
				pageNumber = new
				refreshAll()
			}
			.setContent(guiItems)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val guiText =  GuiText("Your Bazaar Sale Listings", guiWidth = DEFAULT_GUI_WIDTH - 20)
			.addBackground()

		val entryStart = pageNumber * LISTINGS_PER_PAGE
		val entryEnd = ((pageNumber + 1) * LISTINGS_PER_PAGE)
		val showingEntries = items.subList(entryStart, minOf(entryEnd, items.size)) // subList last index is exclusive

		val startLine = 2

		for ((index, bazaarItem) in showingEntries.withIndex()) {
			val line = (index * 2) + startLine
			guiText.add(getMenuTitleName(bazaarItem), line = line, horizontalShift = 20)
			guiText.add(ofChildren(text("P: ", BLACK), bazaarItem.price.toCreditComponent().withShadowColor(BAZAAR_SHADOW_COLOR)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.LEFT)
			guiText.add(ofChildren(text("S: ", BLACK), text(bazaarItem.stock)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.CENTER)
			guiText.add(ofChildren(text("B: ", BLACK), bazaarItem.balance.toCreditComponent().withShadowColor(BAZAAR_SHADOW_COLOR)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.RIGHT)
		}

		val pageNumber = addPageNumber(LISTINGS_PER_PAGE)
		return ofChildren(guiText.build(), pageNumber)
	}

	private fun backingButton(index: Int): AbstractItem {
		val item = object : AbstractItem() {
			val provider = ItemProvider {
				val itemIndex = (pageNumber * LISTINGS_PER_PAGE) + index
				val item = items.getOrNull(itemIndex) ?: return@ItemProvider GuiItem.EMPTY.makeItem(empty())

				val stack = fromItemString(item.itemString)

				val city = cityName(Regions[item.cityTerritory])
				val stock = item.stock
				val uncollected = item.balance.toCreditComponent()
				val price = item.price.toCreditComponent()

				GuiItem.EMPTY.makeItem()
					.updateDisplayName(stack.displayNameComponent)
					.updateLore(listOf(
						ofChildren(template(text("City: {0}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, city)),
						ofChildren(template(text("Stock: {0}", HE_MEDIUM_GRAY), stock)),
						ofChildren(template(text("Balance: {0}", HE_MEDIUM_GRAY), uncollected)),
						ofChildren(template(text("Price: {0}", HE_MEDIUM_GRAY), price))
					))
			}

			override fun getItemProvider(): ItemProvider {
				return provider
			}

			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
		}

		return item.tracked()
	}

	private val gridViewButton = GuiItem.GRID_VIEW.makeItem(text("Grid view")).makeGuiButton { _, _ -> GridListingMenu(viewer, backButtonHandler).openGui() }
}
