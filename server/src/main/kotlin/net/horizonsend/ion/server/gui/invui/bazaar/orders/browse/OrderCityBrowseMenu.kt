package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.kyori.adventure.text.Component
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.eq

class OrderCityBrowseMenu(viewer: Player, city: TradeCityData) : AbstractBrowseMenu(viewer) {
	override val findBson: Bson = BazaarOrder::cityTerritory eq city.territoryId
	override val isGlobalBrowse: Boolean = false

	override val browseName: Component = Component.text(city.displayName)

	override val infoButton: ItemStack = GuiItem.INFO.makeItem(Component.text("TODO"))
}
