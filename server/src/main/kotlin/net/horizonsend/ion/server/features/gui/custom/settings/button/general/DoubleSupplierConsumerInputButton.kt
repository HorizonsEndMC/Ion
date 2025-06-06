package net.horizonsend.ion.server.features.gui.custom.settings.button.general

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.gui.invui.input.validator.RangeDoubleValidator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import java.util.function.Supplier

class DoubleSupplierConsumerInputButton(
	valueSupplier: Supplier<Double>,
	valueConsumer: Consumer<Double>,
	val min: Double,
	val max: Double,
	name: Component,
	description: String,
	icon: GuiItem,
	defaultValue: Double,
) : SupplierConsumerButton<Double>(valueSupplier, valueConsumer, name, description, icon, defaultValue) {
	override fun getSecondLine(player: Player): Component {
		val value = getState(player)
		return text("Current Value: $value", BLUE)
	}

	override fun handleClick(clicker: Player, oldValue: Double, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<Double>) {
		clicker.anvilInputText(
			prompt = text("Enter new value"),
			description = text("Value between $min & $max"),
			backButtonHandler = { parent.openGui() },
			inputValidator = RangeDoubleValidator(min..max),
			handler = { _, (_, result) ->
				newValueConsumer.accept(result.result)
				parent.openGui()
			}
		)
	}
}
