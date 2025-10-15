package net.horizonsend.ion.server.gui.invui.bazaar.terminal.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.ItemBrowseGui
import net.horizonsend.ion.server.gui.invui.bazaar.REMOTE_WARINING
import net.horizonsend.ion.server.gui.invui.bazaar.terminal.browse.grouped.TerminalCityBrowse
import net.horizonsend.ion.server.gui.invui.bazaar.terminal.browse.grouped.TerminalGlobalBrowse
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class TerminalCitySelection(viewer: Player, val entity: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity) : ListInvUIWindow<TradeCityData>(viewer, async = true), ItemBrowseGui<TradeCityData> {
	override fun generateEntries(): List<TradeCityData> = CityNPCs.BAZAAR_CITY_TERRITORIES
		.map { Regions.get<RegionTerritory>(it) }
		.mapNotNull(TradeCities::getIfCity)

	override fun createItem(entry: TradeCityData): Item = formatItem(entry)

	override val listingsPerPage: Int = 27

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . c g . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >"
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('c', citySelectionButton)
			.addIngredient('g', globalBrowseButton)

			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)

			.handlePageChange()
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		return GuiText("Select City")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #"
			)
			.build()
	}

	override fun getItemLore(entry: TradeCityData): List<Component> {
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

	override fun getSearchTerms(entry: TradeCityData): List<String> {
		return listOf(entry.displayName)
	}

	override fun getSearchEntries(): Collection<TradeCityData> {
		return entries
	}

	override fun formatItemStack(entry: TradeCityData): ItemStack {
		return entry.planetIcon.updateDisplayName(text(entry.displayName)).updateLore(getItemLore(entry))
	}
	override fun getItemString(entry: TradeCityData): String = ""

	override fun onClickDisplayedItem(entry: TradeCityData, clickedFrom: CommonGuiWrapper) {
		TerminalCityBrowse(viewer, entity, entry).openGui(this)
	}

	override val isGlobalBrowse: Boolean = false

	override fun goToCitySelection(viewer: Player) {
		TerminalCitySelection(viewer, entity).openGui(parentWindow)
	}

	override fun goToGlobalBrowse(viewer: Player) {
		TerminalGlobalBrowse(viewer, entity).openGui(parentWindow)
	}
}
