package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.changeTitle
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLACK
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil

class ListingMenu(viewer: Player, val backButtonHandler: () -> Unit = {}) : InvUIWindowWrapper(viewer, async = true) {
	companion object {
		private const val LISTINGS_PER_PAGE = 4
	}

	private var pageNumber: Int = 0
	private lateinit var items: List<BazaarItem>

	override fun buildWindow(): Window {
		val items = BazaarItem.find(BazaarItem::seller eq viewer.slPlayerId).toList()
		this.items = items

		val guiItems = items.map { item ->
			val city = cityName(Regions[item.cityTerritory])
			val stock = item.stock
			val uncollected = item.balance.toCreditComponent()
			val price = item.price.toCreditComponent()

			AsyncItem(
				resultProvider = {
					fromItemString(item.itemString)
						.updateLore(listOf(
							ofChildren(template(text("City: {0}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, city)),
							ofChildren(template(text("Stock: {0}", HE_MEDIUM_GRAY), stock)),
							ofChildren(template(text("Balance: {0}", HE_MEDIUM_GRAY), uncollected)),
							ofChildren(template(text("Price: {0}", HE_MEDIUM_GRAY), price))
						))
				},
				handleClick = {
					println("Click: $it")
				}
			)
		}

		val gui = PagedGui.items()
			.setStructure(
				"x . . . . . . . i",
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
			.addIngredient('i', infoButton)
			.addIngredient('0', backingButton(0))
			.addIngredient('1', backingButton(1))
			.addIngredient('2', backingButton(2))
			.addIngredient('3', backingButton(3))
			.addPageChangeHandler { _, new ->
				pageNumber = new
				refreshWindowText()
				updateBackingButtons()
			}
			.setContent(guiItems)
			.build()

		return Window
			.single()
			.setGui(gui)
			.setViewer(viewer)
			.setTitle(buildGuiText())
			.build()
	}

	private fun refreshWindowText() {
		currentWindow?.changeTitle(buildGuiText())
	}

	private fun buildGuiText(): Component {
		val guiText =  GuiText("Your Bazaar Sale Listings", guiWidth = DEFAULT_GUI_WIDTH - 20)
			.addBackground()

		val entryStart = pageNumber * LISTINGS_PER_PAGE
		val entryEnd = ((pageNumber + 1) * LISTINGS_PER_PAGE)
		val showingEntries = items.subList(entryStart, minOf(entryEnd, items.lastIndex))

		val startLine = 2

		for ((index, bazaarItem) in showingEntries.withIndex()) {
			val line = (index * 2) + startLine
			guiText.add(fromItemString(bazaarItem.itemString).displayNameComponent, line = line, horizontalShift = 20)
			guiText.add(ofChildren(text("P: ", BLACK), bazaarItem.price.toCreditComponent()), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.LEFT)
			guiText.add(ofChildren(text("S: ", BLACK), text(bazaarItem.stock)), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.CENTER)
			guiText.add(ofChildren(text("B: ", BLACK), bazaarItem.balance.toCreditComponent()), line = line + 1, horizontalShift = 20, alignment = GuiText.TextAlignment.RIGHT)
		}

		val pageNumber = addPageNumber()
		return ofChildren(guiText.build(), pageNumber)
	}

	private fun addPageNumber(): Component {
		val maxPageNumber = ceil(items.size.toDouble() / (LISTINGS_PER_PAGE.toDouble())).toInt()
		val pageNumberString = "${pageNumber + 1} / $maxPageNumber"

		return GuiText("").add(
			text(pageNumberString),
			line = 10,
			GuiText.TextAlignment.CENTER,
			verticalShift = 4
		).build()
	}

	private val backButton = GuiItem.CANCEL.makeItem(text("Go back")).makeGuiButton { _, _ -> backButtonHandler.invoke() }
	private val infoButton = GuiItem.INFO.makeItem(text("Information")).makeGuiButton { _, _ -> backButtonHandler.invoke() }

	private val backingButtons = mutableListOf<AbstractItem>()
	private fun updateBackingButtons() {
		backingButtons.forEach { it.notifyWindows() }
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

		backingButtons.add(item)

		return item
	}

	private fun cityName(territory: RegionTerritory) = TradeCities.getIfCity(territory)?.displayName
		?: "<{Unknown}>" // this will be used if the city is disbanded but their items remain there
}
