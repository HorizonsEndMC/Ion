package net.horizonsend.ion.server.features.gui.custom.settings.button.general

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.InputValidator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import java.util.function.Supplier

class StringSupplierConsumerInputButton(
	valueSupplier: Supplier<String>,
	valueConsumer: Consumer<String>,
	private val inputDescription: Component = Component.empty(),
	private val validator: InputValidator<String>,
	name: Component,
	buttonDescription: String,
	icon: GuiItem,
	defaultValue: String
) : SupplierConsumerButton<String>(valueSupplier, valueConsumer, name, buttonDescription, icon, defaultValue) {
	override fun getSecondLine(player: Player): Component {
		val value = getState(player)
		return text("Current Value: $value", BLUE)
	}

	override fun handleClick(clicker: Player, oldValue: String, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<String>) {
		clicker.anvilInputText(
			prompt = text("Enter new value"),
			description = inputDescription,
			backButtonHandler = { parent.openGui() },
			inputValidator = validator,
			handler = { _, (_, result) ->
				newValueConsumer.accept(result.result)
				parent.openGui()
			}
		)
	}
}
