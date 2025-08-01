package net.horizonsend.ion.server.features.gui.custom.settings.button.database

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import kotlin.reflect.KMutableProperty1

class DBCachedTwoStateButton(
	name: Component,

	private val trueState: Component,
	private val falseState: Component,

	butonDescription: String,
	icon: GuiItem,
	defaultValue: Boolean,
	db: KMutableProperty1<PlayerSettings, Boolean>,
): DBCachedSettingsButton<Boolean>(name, butonDescription, icon, defaultValue, Boolean::class, db) {
	override fun handleClick(
		clicker: Player,
		oldValue: Boolean,
		gui: PagedGui<*>,
		parent: SettingsPageGui,
		newValueConsumer: Consumer<Boolean>
	) {
		newValueConsumer.accept(!oldValue)
	}

	override fun getSecondLine(player: Player): Component {
		val state = getState(player)
		return if (state) trueState else falseState
	}
}
