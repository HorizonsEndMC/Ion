package net.horizonsend.ion.server.features.gui.custom.settings.button.database

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.button.SettingsMenuButton
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

abstract class DBCachedSettingsButton<T : Any>(
	name: Component,
	butonDescription: String,
	icon: GuiItem,
	defautValue: T,
	val paramType: KClass<T>,
	val db: KMutableProperty1<PlayerSettings, T>
) : SettingsMenuButton<T>(name, butonDescription, icon, defautValue) {
	override fun getState(player: Player): T {
		return PlayerSettingsCache.getSettingOrThrow(player.slPlayerId, db)
	}

	override fun setState(player: Player, state: T) {
		if (!player.isOnline) return

		PlayerSettingsCache.updateSetting(player.slPlayerId, db, state)

		sendUpdateMessage(player, state)
	}

	protected open fun sendUpdateMessage(player: Player, newValue: T) {
		player.success("Set ${name.plainText()} to $newValue")
	}
}
