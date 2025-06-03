package net.horizonsend.ion.server.gui.invui.bazaar.terminal.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setEnumSetting
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.IndividualBrowseGui
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer

class TerminalIndividualBrowseGui(
	viewer: Player,
	private val contextName: Component,
	override val isGlobalBrowse: Boolean,
	private val itemString: String,
	private val parentBson: Bson,
	private val purchaseHandler: (BazaarItem) -> Unit,
	private val openGlobalBrowse: Consumer<Player>,
	private val openCityBrowse: Consumer<Player>
) : ListInvUIWindow<BazaarItem>(viewer, async = true), IndividualBrowseGui<BazaarItem> {
	override val listingsPerPage: Int = 36

	private var sortingMethod: BazaarSort = PlayerSettingsCache.getEnumSettingOrThrow(viewer.slPlayerId, PlayerSettings::defaultBazaarIndividualSort)

	override fun generateEntries(): List<BazaarItem> = BazaarItem.find(and(parentBson, BazaarItem::itemString eq itemString))
		.apply { sortingMethod.sortSellOrders(this) }
		.filter { TradeCities.isCity(Regions[it.cityTerritory]) }

	override fun createItem(entry: BazaarItem): Item = formatItem(entry)

	override fun formatItemStack(entry: BazaarItem): ItemStack {
		return super.formatItemStack(entry).updateDisplayName(entry.price.toCreditComponent())
	}

	override fun getItemLore(entry: BazaarItem): List<Component> = listOf(
		template(text("Seller: {0}", HE_MEDIUM_GRAY), SLPlayer.getName(entry.seller)),
		template(text("Stock: {0}", HE_MEDIUM_GRAY), entry.stock)
	)

	override fun onClickDisplayedItem(entry: BazaarItem) = purchaseHandler.invoke(entry)

	override fun buildWindow(): Window {
		val gui = PagedGui.items().setStructure(
				"x . . c g . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . s . . . S . >"
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('c', citySelectionButton)
			.addIngredient('g', globalBrowseButton)
//			.addIngredient('i', infoButton)

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('s', searchButton)
			.addIngredient('S', sortButton)
			.setContent(items)
			.handlePageChange()
			.build()

		return normalWindow(gui)
	}

	private val sortButton = EnumScrollButton(
		providedItem = { GuiItem.SORT.makeItem(text("Change Sorting Method")) },
		increment = 1,
		value = { sortingMethod },
		enum = BazaarSort::class.java,
		nameFormatter = { it.displayName },
		valueConsumer = {
			sortingMethod = it
			viewer.setEnumSetting(PlayerSettings::defaultBazaarIndividualSort, sortingMethod)
			openGui()
		}
	)

	override fun buildTitle(): Component = GuiText("")
		.setSlotOverlay(
			"# # # # # # # # #",
			". . . . . . . . .",
			". . . . . . . . .",
			". . . . . . . . .",
			"# # # # # # # # #"
		)
		.addBackground(
			GuiText.GuiBackground(
			backgroundChar = BACKGROUND_EXTENDER,
			verticalShift = -11
		))
		.add(getMenuTitleName(fromItemString(itemString)), line = -2, verticalShift = -4)
		.add(ofChildren(contextName), line = -1, verticalShift = -2)
		.build()

	override fun getSearchEntries(): Collection<BazaarItem> = entries

	override fun getSearchTerms(entry: BazaarItem): List<String> {
		val terms = mutableListOf(cityName(Regions[entry.cityTerritory]), entry.itemString, fromItemString(itemString).displayNameString)
		SLPlayer.getName(entry.seller)?.let(terms::add)
		return terms
	}

	override fun goToGlobalBrowse(viewer: Player) {
		openGlobalBrowse.accept(viewer)
	}

	override fun goToCitySelection(viewer: Player) {
		openCityBrowse.accept(viewer)
	}
}
