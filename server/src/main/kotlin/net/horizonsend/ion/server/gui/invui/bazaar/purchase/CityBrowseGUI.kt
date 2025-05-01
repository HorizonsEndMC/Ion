package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.utils.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.setValue

class CityBrowseGUI(parent: BazaarPurchaseMenuParent, val city: TradeCityData) : BrowseGUI(parent) {
	override val searchBson: Bson = and(BazaarItem::stock gt 0, BazaarItem::cityTerritory eq city.territoryId)

	override val searchButton = GuiItem.MAGNIFYING_GLASS
		.makeItem(text("Search for Items"))
		.makeGuiButton { _, _ ->
			println("Search")
		}

	override val sortButton = EnumScrollButton(
		{ GuiItem.GEAR.makeItem(text("Change Sorting Method")) },
		increment = 1,
		value = {
			sortingMethod
		},
		BazaarSort::class.java,
		nameFormatter = { text(it.name) },
		valueConsumer = {
			sortingMethod = it

			PlayerCache[parent.viewer].defaultBazaarSort = sortingMethod.ordinal
			Tasks.async {
				SLPlayer.updateById(parent.viewer.slPlayerId, setValue(SLPlayer::defaultBazaarSort, sortingMethod.ordinal))
			}

			reOpen()
		}
	)

	private fun reOpen() {
		val new = BazaarMainPurchaseMenu(parent.viewer, parent.remote)
		new.openGui()

		if (parent is BazaarMainPurchaseMenu) new.setTab(parent.currentTab)
		new.getTabGUI().setPage(pageNumber)
	}
}
