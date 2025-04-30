package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiItems.closeMenuItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.getCityButtons
import net.horizonsend.ion.server.gui.invui.bazaar.getItemButtons
import net.horizonsend.ion.server.gui.invui.utils.TabButton
import net.horizonsend.ion.server.gui.invui.utils.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

class BazaarMainPurchaseMenu(override val viewer: Player, val remote: Boolean) : InvUIWrapper {
	private fun getMainMenuGui(): Gui {
		return TabGui.normal()
			.setStructure(
				"a . . 1 2 c . d i",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x"
			)
			.addIngredient('a', closeMenuItem(viewer))
			// Switch to city view button
			.addIngredient('1', TabButton(
				GuiItem.MAGNIFYING_GLASS.makeItem()
					.updateLore(listOf(
						text("View list of cities that are selling goods."),
						text("You'll be able to view listings from this menu."),
						Component.empty(),
						text("You currently have this tab selected")
					))
					.updateDisplayName(text("View City Selection")),
				GuiItem.MAGNIFYING_GLASS_GRAY.makeItem()
					.updateLore(listOf(
						text("View list of cities that are selling goods."),
						text("You'll be able to view listings from this menu."),
						Component.empty(),
						text("Click to switch to this tab."),
					))
					.updateDisplayName(text("View City Selection")),
				0
			))
			// Switch to global view button
			.addIngredient('2', TabButton(
				GuiItem.MAGNIFYING_GLASS.makeItem()
					.updateLore(listOf(
						text("View listings from every city, combined"),
						text("into one menu."),
						Component.empty(),
						text("You currently have this tab selected")
					))
					.updateDisplayName(text("View Global Listings")),
				GuiItem.MAGNIFYING_GLASS_GRAY.makeItem()
					.updateLore(listOf(
						text("View listings from every city, combined"),
						text("into one menu."),
						Component.empty(),
						text("Click to switch to this tab."),
					))
					.updateDisplayName(text("View Global Listings")),
				1
			))

			.addIngredient('c', buyOrdersButton)
			.addIngredient('d', settingsButton)
			.addIngredient('i', infoButton)
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setTabs(listOf(getCitySelectionGui(), getGlobalBrowseGui()))
			.build()
	}

	private fun getCitySelectionGui(): Gui {
		val cityButtons = getCityButtons(
			nameBuilder = { city -> text(city.displayName) },
			loreBuilder = { city ->
				val listingCount = BazaarItem.count(BazaarItem::cityTerritory eq city.territoryId)
				val territoryRegion = Regions.get<RegionTerritory>(city.territoryId)

				listOf(
					ofChildren(
						text("Located at ", GRAY), text(territoryRegion.name, AQUA),
						text(" on ", GRAY), text(territoryRegion.world, AQUA), text(".", GRAY)
					),
					text("$listingCount item listing${if (listingCount != 1L) "s" else ""}.")
				)
			},
			clickHandler = { city, _, player ->
				player.information(city.displayName)
			}
		)

		return PagedGui.items()
			.setStructure(
				". . . . . . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >"
			)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(cityButtons)
			.build()
	}

	private fun getGlobalBrowseGui(): Gui {
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

		return PagedGui.items()
			.setStructure(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . s S . . . . >"
			)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('s', globalSearchButton)
			.addIngredient('S', sortButton)
			.setContent(cityButtons)
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

	private fun getMenuTitle(): Component {
		val baseText = if (remote) "Remote Bazaar" else "Bazaar"

		return GuiText(baseText)
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #"
			)
			.build()
	}

	// Start global buttons
	override fun buildWindow(): Window = Window.single()
		.setGui(getMainMenuGui())
		.setTitle(getMenuTitle())
		.setViewer(viewer)
		.build()

	private val buyOrdersButton = GuiItem.CLOCKWISE
		.makeItem(text("Switch to the Buy Order Menu"))
		.makeGuiButton { clickType, player ->
			println("buy orders")
		}

	private val settingsButton = GuiItem.GEAR
		.makeItem(text("Bazaar GUI Settings"))
		.makeGuiButton { clickType, player ->
			println("settings")
		}

	private val infoButton = GuiItem.COUNTERCLOCKWISE
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("Lore Line 1"),
			text("Lore Line 2"),
			text("Lore Line 3"),
		))
		.makeGuiButton { _, _ -> }
}
