package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.listings.IndividualListingGUI
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.Location
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import xyz.xenondevs.invui.item.impl.AbstractItem
import kotlin.math.roundToInt

class GlobalItemListingsMenu(
	viewer: Player,
	remote: Boolean,
	itemString: String,
	private val previousPageNumber: Int? = null,
	pageNumber: Int = 0
) : BazaarPurchaseMenuParent(viewer, remote) {
	override val menuTitle: String = "${fromItemString(itemString).displayNameString} from Everywhere"
	override val contained: IndividualListingGUI = IndividualListingGUI(
		parentWindow = this,
		reOpenHandler = {
			BazaarGUIs.openGlobalItemListings(viewer, remote, itemString, pageNumber)
		},
		searchBson = and(BazaarItem::itemString eq itemString, BazaarItem::stock gt 0),
		itemLoreProvider = { bazaarItem ->
			val region = Regions.get<RegionTerritory>(bazaarItem.cityTerritory)
			val regionWorld = region.bukkitWorld

			val distance = regionWorld?.let { world ->
				if (region.contains(viewer.location)) {
					return@let "0 Blocks"
				}

				val regionCenter = Location(world, region.centerX.toDouble(), 128.toDouble(), region.centerZ.toDouble())

				if (world.uid == viewer.world.uid) {
					return@let "${regionCenter.distance(viewer.location).roundToInt()} Blocks"
				}

				val path = WaypointManager.findShortestPathBetweenLocations(
					regionCenter,
					viewer.location
				)?.vertexList?.mapTo(mutableSetOf()) { it.loc.world } ?: return@let "INCALCULABLE"

				val startingInSpace = viewer.world.ion.hasFlag(WorldFlag.SPACE_WORLD)
				val spaceWorlds = path.count { it.ion.hasFlag(WorldFlag.SPACE_WORLD) }

				if (!startingInSpace && spaceWorlds > 1) return@let "1 Planet Away"

				return@let "${spaceWorlds - 1} Systems Away"
			} ?: "INCALCULABLE"

			listOf(
				template(text("Seller: {0}", GRAY), useQuotesAroundObjects = false, SLPlayer.getName(bazaarItem.seller)),
				template(text("Stock: {0}", GRAY), useQuotesAroundObjects = false, bazaarItem.stock),
				template(text("Sold at {0} on {1}", GRAY), useQuotesAroundObjects = false, TradeCities.getIfCity(region)?.displayName, region.world),
				template(text("Distance: {0}", GRAY), useQuotesAroundObjects = false, distance),
			)
		},
		pageNumber = pageNumber
	)

	override val citySelectionButton: AbstractItem = getCitySelectionButton(true)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(false)

	override val backButton: AbstractItem = GuiItem.LEFT.makeItem(text("Go Back to Viewing Global Listings")).makeGuiButton { _, player ->
		BazaarGUIs.openGlobalBrowse(player, true, previousPageNumber ?: 0)
	}

	override val infoButton: AbstractItem = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("This menu shows individual listings of $itemString at from every city."),
			text("Different players have listed this item for sale, and you can view"),
			text("How much stock their listings have, and the price they have set it at."),
		))
		.makeGuiButton { _, _ -> }

	override fun GuiText.populateGuiText(): GuiText {
		contained.modifyGuiText(this)
		return this
	}
}
