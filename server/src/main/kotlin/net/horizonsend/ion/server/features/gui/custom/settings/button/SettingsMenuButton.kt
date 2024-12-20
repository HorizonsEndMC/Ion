package net.horizonsend.ion.server.features.gui.custom.settings.button

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsGuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer

abstract class SettingsMenuButton<T: Any>(
	val name: Component,
	val butonDescription: String,
	val icon: GuiItem,
	val defautValue: T,
) : SettingsGuiItem {
	override fun getFirstLine(player: Player): Component {
		return name
	}

	abstract fun getState(player: Player): T
	abstract fun setState(player: Player, state: T)

	abstract fun handleClick(clicker: Player, oldValue: T, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<T>)

	override fun makeButton(pageGui: SettingsPageGui): GuiItems.AbstractButtonItem {
		return icon.makeButton(pageGui, name, butonDescription) { player, gui, page ->
			val value = getState(player)

			val newValueHandler = Consumer<T> {
				setState(player, it)
				page.refreshPageText(player, gui)
			}

			handleClick(player, value, gui, page, newValueHandler)
		}
	}
}
