package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.server.features.gui.GuiItem
import net.kyori.adventure.text.Component
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.EMPTY_BSON

class OrderGlobalBrowseMenu(viewer: Player) : AbstractBrowseMenu(viewer) {
	override val findBson: Bson = EMPTY_BSON
	override val isGlobalBrowse: Boolean = true

	override val infoButton: ItemStack = GuiItem.INFO.makeItem(Component.text("TODO"))
}
