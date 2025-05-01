package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.getItemButtons
import net.horizonsend.ion.server.gui.invui.utils.makeGuiButton
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.litote.kmongo.gt
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import kotlin.math.ceil

class GlobalBrowseGUI(override val parent: BazaarPurchaseMenuParent) : InvUIGuiWrapper<Gui>, BazaarGui {
	private var pageNumber = 0
	private var totalItems = 0

	override fun getGui(): Gui {
		val cityButtons = getItemButtons(
			BazaarItem::stock gt 0,
			BazaarSort.PRICE,
			true,
			loreBuilder = { item, filteredItems ->
				val sellers = filteredItems.filter { it.itemString == item }
				val sellerCount = sellers.size
				val totalStock = sellers.sumOf { it.stock }
				val minPrice = sellers.minOfOrNull { it.price } ?: 0
				val maxPrice = sellers.maxOfOrNull { it.price } ?: 0

				listOf(
					template(text("{0} listing${if (sellerCount != 1) "s" else ""} with a total stock of {1}", GRAY), sellerCount, totalStock),
					ofChildren(text("Min price of listing${if (sellerCount != 1) "s" else ""}: ", GRAY), minPrice.toCreditComponent()),
					ofChildren(text("Max price of listing${if (sellerCount != 1) "s" else ""}: ", GRAY), maxPrice.toCreditComponent()),
				)
			},
			clickHandler = { itemString, _, player ->
				player.information(itemString)
			}
		)

		totalItems = cityButtons.size

		return PagedGui.items()
			.setStructure(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . s . . . S . >"
			)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('s', globalSearchButton)
			.addIngredient('S', sortButton)
			.setContent(cityButtons)
			.addPageChangeHandler { old, new ->
				pageNumber = new
				parent.refreshGuiText()
			}
			.build()
	}

	private val globalSearchButton = GuiItem.MAGNIFYING_GLASS
		.makeItem(text("Search for Items"))
		.makeGuiButton { clickType, player ->
			println("Search")
		}

	private val sortButton = GuiItem.STAR
		.makeItem(text("Change Sorting Method"))
		.makeGuiButton { clickType, player ->
			println("Search")
		}

	override fun modifyGuiText(guiText: GuiText) {
		val maxPageNumber = ceil(totalItems.toDouble() / (4.0 * 9.0)).toInt()

		val pageNumberString = "${pageNumber + 1} / $maxPageNumber"
		guiText.add(
			text(pageNumberString),
			line = 10,
			GuiText.TextAlignment.CENTER,
			verticalShift = 4
		)
	}
}
