package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getCityButtons
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

				listOf(
					ofChildren(
						text("Located at ", GRAY), text(territoryRegion.name, AQUA),
						text(" on ", GRAY), text(territoryRegion.world, AQUA), text(".", GRAY)
					),
					template(text("{0} item listing${if (listingCount != 1L) "s" else ""}.", GRAY), listingCount)
				)
			},
			clickHandler = { city, _, player ->
				CityBrowseGUI.getCityBrowse(parent, city).first.openGui()
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
