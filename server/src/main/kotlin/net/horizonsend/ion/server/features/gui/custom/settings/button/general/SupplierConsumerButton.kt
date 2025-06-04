package net.horizonsend.ion.server.features.gui.custom.settings.button.general

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.button.SettingsMenuButton
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.function.Consumer
import java.util.function.Supplier

abstract class SupplierConsumerButton<T : Any>(
	private val valueSupplier: Supplier<T>,
	private val valueConsumer: Consumer<T>,
	name: Component,
	description: String,
	icon: GuiItem,
	defaultValue: T
) : SettingsMenuButton<T>(name, description, icon, defaultValue) {
	override fun setState(player: Player, state: T) {
		valueConsumer.accept(state)
	}

	override fun getState(player: Player): T {
		return valueSupplier.get()
	}
}
