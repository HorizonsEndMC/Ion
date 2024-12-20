package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.gui.GuiItems
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

interface SettingsGuiItem {
	fun getFirstLine(player: Player): Component
	fun getSecondLine(player: Player): Component

	fun makeButton(pageGui: SettingsPageGui): GuiItems.AbstractButtonItem
}
