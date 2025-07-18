package net.horizonsend.ion.server.features.gui.custom.settings.button.general

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import java.util.function.Supplier

class BooleanSupplierConsumerButton(
	valueSupplier: Supplier<Boolean>,
	valueConsumer: Consumer<Boolean>,
	name: Component,
	description: String,
	icon: GuiItem,
	defaultValue: Boolean
) : SupplierConsumerButton<Boolean>(valueSupplier, valueConsumer, name, description, icon, defaultValue) {
	override fun getSecondLine(player: Player): Component {
		val state = getState(player)
		return if (state) text("ENABLED", GREEN) else text("DISABLED", RED)
	}

	override fun handleClick(
		clicker: Player,
		oldValue: Boolean,
		gui: PagedGui<*>,
		parent: SettingsPageGui,
		newValueConsumer: Consumer<Boolean>
	) {
		newValueConsumer.accept(!oldValue)
	}
}
