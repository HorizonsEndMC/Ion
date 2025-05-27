package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.REMOTE_WARINING
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarCitySelectionMenu(viewer: Player) : BazaarPurchaseMenuParent<TradeCityData>(viewer) {
	override val menuTitleLeft: Component = text("Selecting")
	override val menuTitleRight: Component = text("Trade City")

	override val isGlobalBrowse: Boolean = false

	override val listingsPerPage: Int = 27

	override fun getGui(): Gui {
		return PagedGui.items()
			.setStructure(
				". . . . . . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >"
			)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)
			.build()
	}

	override fun generateEntries(): List<TradeCityData> = CityNPCs.BAZAAR_CITY_TERRITORIES
		.map { Regions.get<RegionTerritory>(it) }
		.mapNotNull(TradeCities::getIfCity)

	override fun createItem(entry: TradeCityData): Item = AsyncItem(
		resultProvider = { entry.planetIcon.updateDisplayName(text(entry.displayName)).updateLore(getCityLore(entry)) },
		handleClick = { _ -> handleCityClick(entry) }
	)

	private fun getCityLore(entry: TradeCityData): List<Component> {
		val listingCount = BazaarItem.count(BazaarItem::cityTerritory eq entry.territoryId)
		val territoryRegion = Regions.get<RegionTerritory>(entry.territoryId)

		val lore = listOf(
			ofChildren(
				text("Located at ", HE_MEDIUM_GRAY), text(territoryRegion.name, WHITE),
				text(" on ", HE_MEDIUM_GRAY), text(territoryRegion.world, WHITE), text(".", GRAY)
			),
			template(text("{0} item listing${if (listingCount != 1L) "s" else ""}.", HE_MEDIUM_GRAY), listingCount)
		)

		return if (!territoryRegion.contains(viewer.location)) lore.plus(REMOTE_WARINING) else lore
	}

	private fun handleCityClick(entry: TradeCityData) {
		val remote = !Regions.get<RegionTerritory>(entry.territoryId).contains(viewer.location)
		BazaarGUIs.openCityBrowse(viewer, entry, this)
	}

	override val infoButton: AbstractItem = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("All bazaar listings are made at trade cities, both NPC and player created."),
			text("Players list items for sale at these cities, and you can browse what is being"),
			text("sold at those citites from this menu. If you are not in the territory of"),
			text("the city selling these items, there will be a 4x cost penalty for purchases."),
			empty(),
			text("To view listings from every city in one menu, click the view global listings button"),
			text("button (top center)."),
		))
		.makeGuiButton { _, _ -> }
}
