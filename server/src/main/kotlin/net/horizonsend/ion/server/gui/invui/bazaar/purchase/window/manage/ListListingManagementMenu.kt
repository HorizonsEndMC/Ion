package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.common.utils.text.withShadowColor
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.bazaar.BAZAAR_SHADOW_COLOR
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
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

class ListListingManagementMenu(viewer: Player) : AbstractListingManagementMenu(viewer) {
	override val listingsPerPage: Int = 4

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . . S . . g i",
				"# 0 0 0 0 0 0 0 0",
				"# 1 1 1 1 1 1 1 1",
				"# 2 2 2 2 2 2 2 2",
				"# 3 3 3 3 3 3 3 3",
				"< . . . . . f s >",
			)
			.addIngredient('x', parentOrBackButton())
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
			.setContent(items)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val guiText =  GuiText("Your Bazaar Sale Listings", guiWidth = DEFAULT_GUI_WIDTH - 20)
			.addBackground()

		val startLine = 2

		for ((index, bazaarItem) in getDisplayedEntries().withIndex()) {
			val line = (index * 2) + startLine
			guiText.add(getMenuTitleName(bazaarItem), line = line, horizontalShift = 20)
			guiText.add(ofChildren(text("P: ", BLACK), bazaarItem.price.toCreditComponent().withShadowColor(BAZAAR_SHADOW_COLOR)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.LEFT)
			guiText.add(ofChildren(text("S: ", BLACK), text(bazaarItem.stock)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.CENTER)
			guiText.add(ofChildren(text("B: ", BLACK), bazaarItem.balance.toCreditComponent().withShadowColor(BAZAAR_SHADOW_COLOR)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.RIGHT)
		}

		return withPageNumber(guiText)
	}

	private fun backingButton(index: Int): AbstractItem {
		val item = object : AbstractItem() {
			val provider = ItemProvider {
				val bazaarItem = getDisplayedEntries().getOrNull(index) ?: return@ItemProvider GuiItem.EMPTY.makeItem(empty())

				val stack = fromItemString(bazaarItem.itemString)

				val city = cityName(Regions[bazaarItem.cityTerritory])
				val stock = bazaarItem.stock
				val uncollected = bazaarItem.balance.toCreditComponent()
				val price = bazaarItem.price.toCreditComponent()

				GuiItem.EMPTY.makeItem()
					.updateDisplayName(stack.displayNameComponent)
					.updateLore(listOf(
						ofChildren(template(text("City: {0}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, city)),
						ofChildren(template(text("Stock: {0}", HE_MEDIUM_GRAY), stock)),
						ofChildren(template(text("Balance: {0}", HE_MEDIUM_GRAY), uncollected)),
						ofChildren(template(text("Price: {0}", HE_MEDIUM_GRAY), price))
					))
			}

			override fun getItemProvider(): ItemProvider = provider

			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
				val bazaarItem = getDisplayedEntries().getOrNull(index) ?: return
				handleEntryClick(bazaarItem)
			}
		}

		return item.tracked()
	}

	private val gridViewButton = GuiItem.GRID_VIEW.makeItem(text("Switch to Grid view")).makeGuiButton { _, _ ->
		viewer.setSetting(PlayerSettings::listingManageDefaultListView, false)
		BazaarGUIs.openListingManageGridMenu(viewer, parentWindow)
	}
}
