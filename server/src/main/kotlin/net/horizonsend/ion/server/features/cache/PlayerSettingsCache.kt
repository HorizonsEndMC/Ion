package net.horizonsend.ion.server.features.cache

import net.horizonsend.ion.common.database.cache.AbstractPlayerSettingsCache
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.litote.kmongo.eq
import kotlin.reflect.KMutableProperty1

object PlayerSettingsCache : AbstractPlayerSettingsCache() {
	override fun kickId(player: SLPlayerId, reason: Component) {
		Bukkit.getPlayer(player.id)?.kick(reason)
	}

	override fun runAsync(task: () -> Unit) {
		Tasks.async(task)
	}

	fun migrateLegacySettings() {
		IonServer.slF4JLogger.info("MIGRATING PLAYER SETTINGS START\n\n\n")
		for (slPlayerId in SLPlayer.allIds()) {
			for ((legacy, modern) in mapped) {
				if (modern == null) continue
				val existing = SLPlayer.findOneProp(SLPlayer::_id eq slPlayerId, legacy) ?: continue
				updateSetting(slPlayerId, modern, existing)
			}
		}
		IonServer.slF4JLogger.info("\n\n\nMIGRATING PLAYER SETTINGS FINISHED")
	}

	override fun load() {
		if (PlayerSettings.allIds().none()) {
			migrateLegacySettings()
		}

		listen<PlayerQuitEvent> { event ->
			callOnQuit(event.player.slPlayerId)
		}

		listen<AsyncPlayerPreLoginEvent>(priority = EventPriority.MONITOR, ignoreCancelled = true) { event ->
			callOnPreLogin(event.uniqueId.slPlayerId)
		}

		listen<PlayerJoinEvent>(priority = EventPriority.LOWEST) { event ->
			callOnLoginLow(event.player.slPlayerId)
		}
	}

	operator fun <T : Any> get(player: Player, settingProperty: KMutableProperty1<PlayerSettings, T>): T = getSettingOrThrow(player.slPlayerId, settingProperty)
	inline operator fun <reified T : Enum<T>> get(player: Player, settingProperty: KMutableProperty1<PlayerSettings, Int>): T = getEnumSettingOrThrow(player.slPlayerId, settingProperty)

	operator fun <T : Any> set(player: Player, settingProperty: KMutableProperty1<PlayerSettings, T>, newValue: T) = updateSetting(player.slPlayerId, settingProperty, newValue)
	operator fun <T : Enum<T>> set(player: Player, settingProperty: KMutableProperty1<PlayerSettings, Int>, newValue: T) = updateEnumSetting(player.slPlayerId, settingProperty, newValue)

	fun <T : Any>  Player.getSetting(settingProperty: KMutableProperty1<PlayerSettings, T>) = get(this, settingProperty)
	inline fun <reified T : Enum<T>>  Player.getSetting(settingProperty: KMutableProperty1<PlayerSettings, Int>) = get<T>(this, settingProperty)

	fun <T : Any>  Player.setSetting(settingProperty: KMutableProperty1<PlayerSettings, T>, newValue: T) = set(this, settingProperty, newValue)
	inline fun <reified T : Enum<T>>  Player.setSetting(settingProperty: KMutableProperty1<PlayerSettings, Int>, newValue: T) = set(this, settingProperty, newValue)
}
