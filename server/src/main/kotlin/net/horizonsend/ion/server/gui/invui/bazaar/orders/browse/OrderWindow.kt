package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.inventory.ItemStack

interface OrderWindow : CommonGuiWrapper {
	val isGlobalBrowse: Boolean

	val citySelectionButton get() =
		(if (!isGlobalBrowse) GuiItem.CITY.makeItem(text("Go to city selection")).updateLore(listOf(text("You already have this menu selected.")))
		else GuiItem.CITY_GRAY.makeItem(text("Go to city selection"))).makeGuiButton { _, player -> OrderCitySelection(player).openGui(this) }

	val globalBrowseButton get() =
		(if (isGlobalBrowse) GuiItem.WORLD .makeItem(text("Go to global browse")).updateLore(listOf(text("You already have this menu selected.")))
		else GuiItem.WORLD_GRAY.makeItem(text("Go to global browse"))).makeGuiButton { _, player -> OrderGlobalBrowseMenu(player).openGui(this) }

	val listingBrowseButton get() = GuiItem.DOWN.makeItem(text("View Bazaar Sale Listings")).makeGuiButton { _, player -> BazaarGUIs.openCitySelection(player, true, this) }

	val infoButton: ItemStack

	val settingsButton get() = GuiItem.GEAR.makeItem(text("Open Bazaar Settings")).makeGuiButton { _, player -> BazaarGUIs.openBazaarSettings(player, this) }
}
