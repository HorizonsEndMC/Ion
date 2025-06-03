package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

interface BrowseGui : CommonGuiWrapper {
	val isGlobalBrowse: Boolean

	val citySelectionButton get() =
		(if (!isGlobalBrowse) GuiItem.CITY.makeItem(text("Go to city selection")).updateLore(listOf(text("You already have this menu selected.")))
		else GuiItem.CITY_GRAY.makeItem(text("Go to city selection"))).makeGuiButton { _, viewer -> goToCitySelection(viewer) }

	fun goToCitySelection(viewer: Player)

	val globalBrowseButton get() =
		(if (isGlobalBrowse) GuiItem.WORLD .makeItem(text("Go to global browse")).updateLore(listOf(text("You already have this menu selected.")))
		else GuiItem.WORLD_GRAY.makeItem(text("Go to global browse"))).makeGuiButton { _, viewer -> goToGlobalBrowse(viewer) }

	fun goToGlobalBrowse(viewer: Player)
}
