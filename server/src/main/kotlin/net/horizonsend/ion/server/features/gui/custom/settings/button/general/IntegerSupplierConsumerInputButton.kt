package net.horizonsend.ion.server.features.gui.custom.settings.button.general

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeIntegerValidator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import java.util.function.Supplier

class IntegerSupplierConsumerInputButton(
	valueSupplier: Supplier<Int>,
	valueConsumer: Consumer<Int>,
	val min: Int,
	val max: Int,
	name: Component,
	description: String,
	icon: GuiItem,
	defaultValue: Int
) : SupplierConsumerButton<Int>(valueSupplier, valueConsumer, name, description, icon, defaultValue) {
	override fun getSecondLine(player: Player): Component {
		val value = getState(player)
		return text("Current Value: $value", BLUE)
	}

	override fun handleClick(clicker: Player, oldValue: Int, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<Int>) {
		clicker.anvilInputText(
			prompt = text("Enter new value"),
			description = text("Value between $min & $max"),
			backButtonHandler = { parent.openGui() },
			inputValidator = RangeIntegerValidator(min..max),
			handler = { _, (_, result) ->
				newValueConsumer.accept(result.result)
				parent.openGui()
			}
		)
	}
}
