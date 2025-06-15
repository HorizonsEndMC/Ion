package net.horizonsend.ion.common.database.cache

import com.google.common.collect.HashBasedTable
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.utils.set
import net.horizonsend.ion.common.utils.text.miniMessage
import net.kyori.adventure.text.Component
import org.litote.kmongo.id.StringId
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import org.litote.kmongo.upsert
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

abstract class AbstractPlayerSettingsCache : Cache {
	companion object {
		private val legacySettingProperties = SLPlayer::class.memberProperties.filterIsInstance<KMutableProperty1<SLPlayer, Any>>()

		private val settingProperties = PlayerSettings::class.memberProperties.filterIsInstance<KMutableProperty1<PlayerSettings, Any>>()

		val mapped = legacySettingProperties.associateWith { legacy -> settingProperties.firstOrNull { modern -> legacy.name == modern.name } }
	}

	private val settingsTable = HashBasedTable.create<SLPlayerId, KMutableProperty1<PlayerSettings, out Any>, Any>()

	abstract fun kickId(player: SLPlayerId, reason: Component)

	abstract fun runAsync(task: () -> Unit)

	fun removePlayer(player: SLPlayerId) {
		settingsTable.rowMap().remove(player)
	}

	private fun <T : Any> updateStoredSetting(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, T>, newValue: T) {
		settingsTable[player, settingProperty] = newValue
	}

	fun <T : Any> updateSetting(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, T>, newValue: T) {
		updateStoredSetting(player, settingProperty, newValue)
		runAsync {
			PlayerSettings.col.updateOneById(player as StringId<PlayerSettings>, setValue(settingProperty, newValue), upsert())
		}
	}

	operator fun <T : Any> set(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, T>, newValue: T) {
		updateSetting(player, settingProperty, newValue)
	}

	fun <T : Enum<T>> updateEnumSetting(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, Int>, newValue: T) {
		updateSetting(player, settingProperty, newValue.ordinal)
	}

	fun <T : Any> getIfOnline(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, T>) : T? {
		@Suppress("UNCHECKED_CAST")
		return settingsTable[player, settingProperty] as T?
	}

	inline fun <reified T : Enum<T>> getEnumSetting(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, Int>) : T? {
		val ordinal = getIfOnline(player, settingProperty) ?: return null
		return T::class.java.enumConstants[ordinal]
	}

	inline fun <reified T : Enum<T>> getEnumSettingOrThrow(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, Int>) : T {
		val ordinal = getIfOnline(player, settingProperty) ?: return null!!
		return T::class.java.enumConstants[ordinal]
	}

	operator fun <T : Any> get(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, T>): T = getSettingOrThrow(player, settingProperty)

	fun <T : Any> getSettingOrThrow(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, T>) : T {
		@Suppress("UNCHECKED_CAST")
		return settingsTable[player, settingProperty] as T
	}

	fun isCached(player: SLPlayerId): Boolean = settingsTable.rowKeySet().contains(player)

	fun cache(player: SLPlayerId): Future<Boolean> {
		val future = CompletableFuture<Boolean>()

		runAsync {
			val settings = PlayerSettings.findById(player as StringId<PlayerSettings>)

			if (settings == null) {
				future.complete(false)
				return@runAsync
			}

			for (setting in settingProperties) {
				updateStoredSetting(player, setting, setting.getter.call(settings))
			}
		}

		return future
	}

	// priority monitor so it happens after the insert in SLCore, which is at HIGHEST
	fun callOnPreLogin(id: SLPlayerId) {
		cache(id)
	}

	// lowest in case anything uses this data in other join listeners, and since lowest join event
	// is always after monitor async player pre login event, though the async player pre login event
	// may not be called if the plugin is not registered when they initiate authentication, like in a restart
	fun callOnLoginLow(id: SLPlayerId) {
		if (!settingsTable.rowKeySet().contains(id)) {
			kickId(id, "<red>Failed to load data! Please try again.".miniMessage())
		}
	}

	fun callOnQuit(id: SLPlayerId) {
		removePlayer(id)
	}
}
