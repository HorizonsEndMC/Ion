package net.horizonsend.ion.server.features.gui.custom.settings.button.general

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openSearchMenu
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import java.util.function.Supplier

class RegistryKeyConsumerInputButton<T : Any>(
	private val keyRegistry: KeyRegistry<T>,
	private val keyFilter: (IonRegistryKey<T, out T>) -> Boolean,
	valueSupplier: Supplier<IonRegistryKey<T, out T>?>,
	valueConsumer: Consumer<IonRegistryKey<T, out T>?>,
	private val inputDescription: Component = Component.empty(),
	name: Component,
	buttonDescription: String,
	icon: GuiItem,
	defaultValue: IonRegistryKey<T, out T>?,
	private val searchTermProvider: (IonRegistryKey<T, out T>) -> Collection<String> = { listOf(it.key) }
) : SupplierConsumerButton<IonRegistryKey<T, out T>?>(valueSupplier, valueConsumer, name, buttonDescription, icon, defaultValue) {
	override fun getSecondLine(player: Player): Component {
		val value = getState(player)
		return text("Current: ${value?.key}", BLUE)
	}

	override fun handleClick(clicker: Player, oldValue: IonRegistryKey<T, out T>?, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<IonRegistryKey<T, out T>?>) {
		clicker.openSearchMenu(
			keyRegistry.allkeys().filter(keyFilter),
			searchTermProvider,
			inputDescription,
			backButtonHandler = { parent.openGui() },
			handler = { _, result ->
				newValueConsumer.accept(result)
				parent.openGui()
			}
		)
	}
}
