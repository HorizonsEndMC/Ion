package net.horizonsend.ion.server.gui.invui.bazaar.orders.manage

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.text.BAZAAR_ORDER_MANAGE_HEADER_ICON
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.common.utils.text.withShadowColor
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.bazaar.BAZAAR_SHADOW_COLOR
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLACK
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

class ListOrderManagementMenu(
	viewer: Player,
	handleListingClick: AbstractOrderManagementMenu.(Oid<BazaarOrder>) -> Unit = { BazaarGUIs.openBuyOrderEditorMenu(viewer, it, this) }
) : AbstractOrderManagementMenu(viewer, handleListingClick) {
	override val listingsPerPage: Int = 4

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"b . . . p . . g i",
				"# z z z z z z z z",
				"# y y y y y y y y",
				"# x x x x x x x x",
				"# w w w w w w w w",
				"l . S . . . f s r",
			)
			.addIngredient('f', filterButton)
			.addIngredient('s', sortButton)
			.addIngredient('b', parentOrBackButton())
			.addIngredient('g', switchLayoutButton)
			.addIngredient('i', infoButton)

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('l', GuiItems.PageLeftItem())
			.addIngredient('S', searchButton)
			.addIngredient('r', GuiItems.PageRightItem())
			.addIngredient('p', createBuyOrderMenu)

			.addIngredient('z', backingButton(0))
			.addIngredient('y', backingButton(1))
			.addIngredient('x', backingButton(2))
			.addIngredient('w', backingButton(3))
			.addPageChangeHandler { _, new ->
				pageNumber = new
				refreshAll()
			}

			.setContent(items)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText("", guiWidth = DEFAULT_GUI_WIDTH - 20)
			.addBorder(GuiBorder.regular(
				color = HE_LIGHT_ORANGE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_ORDER_MANAGE_HEADER_ICON, 48, HE_LIGHT_ORANGE),
				leftText = text("Manage"),
				rightText = text("Buy Orders")
			))
			.addBackground()

		val startLine = 2

		for ((index, bazaarItem) in getDisplayedEntries().withIndex()) {
			val line = (index * 2) + startLine
			text.add(getMenuTitleName(fromItemString(bazaarItem.itemString)), line = line, horizontalShift = 20)
			text.add(ofChildren(text("P: ", BLACK), bazaarItem.pricePerItem.toCreditComponent().withShadowColor(BAZAAR_SHADOW_COLOR)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.LEFT)
			text.add(ofChildren(text("S: ", BLACK), text(bazaarItem.stock)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.CENTER)
			text.add(ofChildren(text("B: ", BLACK), bazaarItem.balance.toCreditComponent().withShadowColor(BAZAAR_SHADOW_COLOR)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.RIGHT)
		}

		return withPageNumber(text)
	}

	override val switchLayoutButton: Item = GuiItem.GRID_VIEW.makeItem(text("Switch to Grid Layout")).makeGuiButton { _, _ ->
		viewer.setSetting(PlayerSettings::orderManageDefaultListView, false)
		GridOrderManagementWindow(viewer, handleListingClick).openGui(parentWindow)
	}

	private fun backingButton(index: Int): AbstractItem {
		val item = object : AbstractItem() {
			val provider = ItemProvider {
				val order = getDisplayedEntries().getOrNull(index) ?: return@ItemProvider GuiItem.EMPTY.makeItem(empty())
				val stack = fromItemString(order.itemString)

				GuiItem.EMPTY.makeItem()
					.updateDisplayName(stack.displayNameComponent)
					.applyItemFormatting(order)
			}

			override fun getItemProvider(): ItemProvider = provider

			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
				val order = getDisplayedEntries().getOrNull(index) ?: return
				handleListingClick.invoke(this@ListOrderManagementMenu, order._id)
			}
		}

		return item.tracked()
	}
}
