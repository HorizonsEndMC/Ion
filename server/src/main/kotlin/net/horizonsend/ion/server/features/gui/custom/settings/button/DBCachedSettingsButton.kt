package net.horizonsend.ion.server.features.gui.custom.settings.button

import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.litote.kmongo.setValue
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

abstract class DBCachedSettingsButton<T : Any>(
	name: Component,
	butonDescription: String,
	icon: GuiItem,
	defautValue: T,
	val paramType: KClass<T>,
	val db: KMutableProperty1<SLPlayer, T>,
	val cache: KMutableProperty1<AbstractPlayerCache.PlayerData, T>
) : SettingsMenuButton<T>(name, butonDescription, icon, defautValue) {
	override fun getState(player: Player): T {
		val playerData = PlayerCache.getIfOnline(player)
		return if (playerData != null) { cache.get(playerData) } else SLPlayer.findPropById(player.slPlayerId, paramType, db) ?: defautValue
	}

	override fun setState(player: Player, state: T) {
		val cached = PlayerCache.getIfOnline(player)

		if (cached != null) {
			cache.set(cached, state)
		}

		Tasks.async {
			SLPlayer.updateById(player.slPlayerId, setValue(db, state))
		}

		sendUpdateMessage(player, state)
	}

	protected open fun sendUpdateMessage(player: Player, newValue: T) {
		player.success("Set ${name.plainText()} to $newValue")
	}
}
