package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.REMOTE_WARINING
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >",
			)
			.addIngredient('x', parentOrBackButton())

			.addIngredient('c', citySelectionButton)
			.addIngredient('g', globalBrowseButton)
			.addIngredient('b', listingBrowseButton)

			.addIngredient('o', settingsButton)
			.addIngredient('i', infoButton)

			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)

		return normalWindow(gui.build())
	}

	override fun buildTitle(): Component {
		return GuiText("Select City")
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
	override val infoButton: ItemStack = GuiItem.INFO.makeItem(Component.text("TODO"))
}
