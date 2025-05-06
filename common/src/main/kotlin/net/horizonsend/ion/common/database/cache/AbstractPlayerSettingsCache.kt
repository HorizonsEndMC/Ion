package net.horizonsend.ion.common.database.cache

import com.google.common.collect.HashBasedTable
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
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
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

abstract class AbstractPlayerSettingsCache : Cache {
	companion object {
		private val settingProperties = PlayerSettings::class.memberProperties.filterIsInstance<KMutableProperty1<PlayerSettings, Any>>()
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

	fun <T : Enum<T>> updateEnumSetting(player: SLPlayerId, settingProperty: KMutableProperty1<PlayerSettings, Int>, newValue: T) {
		updateSetting(player, settingProperty, newValue.ordinal)
	}

	fun <T : Any> getSetting(player: SLPlayerId, settingProperty: KMutableProperty<T>) : T? {
		@Suppress("UNCHECKED_CAST")
		return settingsTable[player, settingProperty] as T?
	}

	fun <T : Any> getSettingOrThrow(player: SLPlayerId, settingProperty: KMutableProperty<T>) : T {
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
