package net.horizonsend.ion.server.features.gui.custom.settings.button.general

import net.horizonsend.ion.common.utils.text.clip
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.restrictedMiniMessageSerializer
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import java.util.function.Supplier

class ComponentSupplierConsumerInputButton(
	valueSupplier: Supplier<Component>,
	valueConsumer: Consumer<Component>,
	private val inputDescription: Component = Component.empty(),
	name: Component,
	buttonDescription: String,
	icon: GuiItem,
	defaultValue: Component
) : SupplierConsumerButton<Component>(valueSupplier, valueConsumer, name, buttonDescription, icon, defaultValue) {
	override fun getSecondLine(player: Player): Component {
		val value = getState(player)
		return template(text("Current: {0}", BLUE), value.clip(100))
	}

	override fun handleClick(clicker: Player, oldValue: Component, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<Component>) {
		clicker.openInputMenu(
			prompt = text("Enter new value"),
			description = inputDescription,
			backButtonHandler = { parent.openGui() },
			inputValidator = {
				val deserialized = restrictedMiniMessageSerializer.deserializeOrNull(it)

				if (deserialized != null && deserialized.plainText().isEmpty()) return@openInputMenu ValidatorResult.FailureResult(text("Result must not be empty!", RED))

				if (deserialized != null) ValidatorResult.ValidatorSuccessSingleEntry(deserialized)
				else ValidatorResult.FailureResult(text("Could not deserialize component!", RED))
			},
			componentTransformer = { it },
			handler = { _, result ->
				newValueConsumer.accept(result.result)
				parent.openGui()
			}
		)
	}
}
