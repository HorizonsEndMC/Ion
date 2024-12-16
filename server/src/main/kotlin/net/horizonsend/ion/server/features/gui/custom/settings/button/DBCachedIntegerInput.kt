package net.horizonsend.ion.server.features.gui.custom.settings.button

import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.RangeIntegerValidator
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
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
	db: KMutableProperty1<SLPlayer, Int>,
	cache: KMutableProperty1<AbstractPlayerCache.PlayerData, Int>,
) : DBCachedSettingsButton<Int>(name, butonDescription, icon, defaultValue, Int::class, db, cache) {
	override fun getSecondLine(player: Player): Component {
		val value = getState(player)
		return text("Current Value: $value", BLUE)
	}

	override fun handleClick(clicker: Player, oldValue: Int, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<Int>) {
		clicker.anvilInputText(
			prompt = text("Enter new value"),
			description = text("Value between $min & $max"),
			backButtonHandler = { parent.open() },
			inputValidator = RangeIntegerValidator(min..max),
			handler = {
				newValueConsumer.accept(it.toIntOrNull() ?: return@anvilInputText)
				parent.open()
			}
		)
	}
}
