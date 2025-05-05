package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.economy.BazaarCommand.cityName
import net.horizonsend.ion.server.features.gui.custom.misc.ItemMenu
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.eq

class ListingMenu(private val viewer: Player, val backButtonHandler: () -> Unit = {}) : CommonGuiWrapper {
	override fun openGui() {
		Tasks.async {
			val items = BazaarItem.find(BazaarItem::seller eq viewer.slPlayerId).toList()

			val guiItems = items.map { item ->
				val city = cityName(Regions[item.cityTerritory])
				val stock = item.stock
				val uncollected = item.balance.toCreditComponent()
				val price = item.price.toCreditComponent()

				AsyncItem(
					resultProvider = {
						fromItemString(item.itemString)
							.updateLore(listOf(
								ofChildren(template(text("City: {0}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, city)),
								ofChildren(template(text("Stock: {0}", HE_MEDIUM_GRAY), stock)),
								ofChildren(template(text("Balance: {0}", HE_MEDIUM_GRAY), uncollected)),
								ofChildren(template(text("Price: {0}", HE_MEDIUM_GRAY), price))
							))
					},
					handleClick = {
						println("Click: $it")
					}
				)
			}
			Tasks.sync {
				ItemMenu(
					title = text("Your Bazaar Listings"),
					viewer = viewer,
					guiItems = guiItems,
					backButtonHandler = { backButtonHandler.invoke() }
				).openGui()
			}
		}
	}
}
