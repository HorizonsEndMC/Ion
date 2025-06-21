package net.horizonsend.ion.server.features.gui.custom.settings.button.database

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeIntegerValidator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import kotlin.reflect.KMutableProperty1

class DBCachedIntegerInput(
	val min: Int,
	val max: Int,
	name: Component,
	butonDescription: String,
	icon: GuiItem,
	defaultValue: Int,
	db: KMutableProperty1<PlayerSettings, Int>,
) : DBCachedSettingsButton<Int>(name, butonDescription, icon, defaultValue, Int::class, db) {
	override fun getSecondLine(player: Player): Component {
		val value = getState(player)
		return text("Current Value: $value", BLUE)
	}

	override fun handleClick(clicker: Player, oldValue: Int, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<Int>) {
		clicker.openInputMenu(
			prompt = text("Enter new value"),
			description = text("Value between $min & $max"),
			backButtonHandler = { parent.openGui() },
			inputValidator = RangeIntegerValidator(min..max),
			handler = { _, result ->
				newValueConsumer.accept(result.result)
				parent.openGui()
			}
		)
	}
}
