package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.REMOTE_WARINING
import net.horizonsend.ion.server.gui.invui.bazaar.getCityButtons
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers

class CitySelectionGUI(val parent: BazaarPurchaseMenuParent) : InvUIGuiWrapper<Gui> {
	override fun getGui(): Gui {
		val cityButtons = getCityButtons(
			nameBuilder = { city -> text(city.displayName) },
			loreBuilder = { city ->
				val listingCount = BazaarItem.count(BazaarItem::cityTerritory eq city.territoryId)
				val territoryRegion = Regions.get<RegionTerritory>(city.territoryId)

				val lore = listOf(
					ofChildren(
						text("Located at ", HE_MEDIUM_GRAY), text(territoryRegion.name, AQUA),
						text(" on ", HE_MEDIUM_GRAY), text(territoryRegion.world, AQUA), text(".", GRAY)
					),
					template(text("{0} item listing${if (listingCount != 1L) "s" else ""}.", HE_MEDIUM_GRAY), listingCount)
				)

				if (!territoryRegion.contains(parent.viewer.location)) lore.plus(REMOTE_WARINING) else lore
			},
			clickHandler = { city, _, player ->
				val remote = !Regions.get<RegionTerritory>(city.territoryId).contains(player.location)
				BazaarGUIs.openCityBrowse(player, remote, city)
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
}
