package net.horizonsend.ion.server.features.gui.custom.settings.button

import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.function.Consumer
import kotlin.reflect.KMutableProperty1

class DBCachedEnumCycle<T : Enum<T>>(
	private val enum: Class<T>,
	name: Component,
	butonDescription: String,
	icon: GuiItem,
	defaultValue: Int,
	db: KMutableProperty1<SLPlayer, Int>,
	cache: KMutableProperty1<AbstractPlayerCache.PlayerData, Int>,
) : DBCachedSettingsButton<Int>(name, butonDescription, icon, defaultValue, Int::class, db, cache) {
	override fun getSecondLine(player: Player): Component {
		val value = getState(player)
		return text(enum.enumConstants[value].name, BLUE)
	}

	override fun handleClick(clicker: Player, oldValue: Int, gui: PagedGui<*>, parent: SettingsPageGui, newValueConsumer: Consumer<Int>) {
		val lastIndex = enum.enumConstants.lastIndex
		val newSetting = if (oldValue < lastIndex) oldValue + 1 else 0

		newValueConsumer.accept(newSetting)
	}

	override fun sendUpdateMessage(player: Player, newValue: Int) {
		player.success("Set ${name.plainText()} to ${enum.enumConstants[newValue].name}")
	}
}
