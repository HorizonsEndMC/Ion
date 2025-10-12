package net.horizonsend.ion.server.features.gui.custom.settings.button

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsGuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class ArbitraryButton(
	val name: String,
	val firstLine: Component,
	val secondLine: Component,
	val icon: GuiItem,
	val handleClick: (Player, SettingsPageGui) -> Unit
) : SettingsGuiItem {
	override fun getFirstLine(player: Player): Component = firstLine
	override fun getSecondLine(player: Player): Component = secondLine

	override fun makeButton(pageGui: SettingsPageGui): GuiItems.AbstractButtonItem {
		return icon.makeButton(pageGui, name, "") { player, gui, page ->
			handleClick(player, page)
		}
	}
}
