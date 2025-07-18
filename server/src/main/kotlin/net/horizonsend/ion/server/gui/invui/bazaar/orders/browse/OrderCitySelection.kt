package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.utils.text.BAZAAR_ORDER_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.REMOTE_WARINING
import net.horizonsend.ion.server.gui.invui.bazaar.getBazaarSettingsButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class OrderCitySelection(viewer: Player) : ListInvUIWindow<TradeCityData>(viewer, async = true), OrderWindow {
	override val listingsPerPage: Int = 36

	override fun createItem(entry: TradeCityData): Item = AsyncItem(
		resultProvider = { entry.planetIcon.updateDisplayName(text(entry.displayName)).updateLore(getCityItemLore(entry)) },
		handleClick = { _ -> OrderCityBrowseMenu(viewer, entry).openGui(this) }
	)

	private fun getCityItemLore(city: TradeCityData): List<Component> {
		val listingCount = BazaarOrder.count(BazaarOrder::cityTerritory eq city.territoryId)
		val territoryRegion = Regions.get<RegionTerritory>(city.territoryId)

		val lore = listOf(
			template(text("Located at {0} on {1}.", HE_MEDIUM_GRAY), territoryRegion.name, territoryRegion.world, WHITE),
			template(text("{0} orders listing${if (listingCount != 1L) "s" else ""}.", HE_MEDIUM_GRAY), listingCount)
		)

		return if (!territoryRegion.contains(viewer.location)) lore.plus(REMOTE_WARINING) else lore
	}

	override fun generateEntries(): List<TradeCityData> = CityNPCs.BAZAAR_CITY_TERRITORIES
		.map { Regions.get<RegionTerritory>(it) }
		.mapNotNull(TradeCities::getIfCity)

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . c g b . o i",
				". . . . . . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >",
			)
			.addIngredient('x', parentOrBackButton())

			.addIngredient('c', citySelectionButton)
			.addIngredient('g', globalBrowseButton)
			.addIngredient('b', listingBrowseButton)

			.addIngredient('o', getBazaarSettingsButton())
			.addIngredient('i', infoButton)

			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)

		return normalWindow(gui.build())
	}

	override fun buildTitle(): Component {
		return GuiText("")
			.addBorder(GuiBorder.regular(
				color = HE_LIGHT_ORANGE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_ORDER_HEADER_ICON, 48, HE_LIGHT_ORANGE),
				leftText = text("Selecting"),
				rightText = text("Trade City")
			))
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
			).
			build()
	}

	override val isGlobalBrowse: Boolean = false
	override val infoButton = makeInformationButton(
		title = text("Information"),

		text("All bazaar orders are made at trade cities, both NPC and player created."),
		text("Players order items at these cities, and you can browse what is being"),
		text("ordered at those citites from this menu."),
		empty(),
		text("To view orders from every city in one menu, click the view global listings button"),
		text("button (top center).")
	)

	override fun getItemLore(entry: BazaarOrder): List<Component> = listOf()
	override fun onClickDisplayedItem(entry: BazaarOrder) {}

	override fun getSearchTerms(entry: BazaarOrder): List<String> {
		TODO("Not yet implemented")
	}

	override fun getSearchEntries(): Collection<BazaarOrder> {
		TODO("Not yet implemented")
	}
}
