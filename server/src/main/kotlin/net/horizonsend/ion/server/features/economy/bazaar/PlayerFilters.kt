package net.horizonsend.ion.server.features.economy.bazaar

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.economy.bazaar.BazaarFilter.CityBlacklist
import net.horizonsend.ion.server.features.economy.bazaar.BazaarFilter.CityWhitelist
import net.horizonsend.ion.server.features.economy.bazaar.BazaarFilter.PlayerBlacklist
import net.horizonsend.ion.server.features.economy.bazaar.BazaarFilter.PlayerWhitelist
import org.bukkit.entity.Player
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

@Serializable
data class PlayerFilters(var filters: List<BazaarFilter> = listOf()) {
	fun save(player: Player, setting: KMutableProperty1<PlayerSettings, String>) = player.setSetting(setting, Configuration.write(this))

	fun matches(item: BazaarItem): Boolean {
		return filters.all { it.matches(item) }
	}
	fun matches(order: BazaarOrder): Boolean {
		return filters.all { it.matches(order) }
	}

	companion object {
		fun get(player: Player, setting: KMutableProperty1<PlayerSettings, String>) = Configuration.parse<PlayerFilters>(player.getSetting(setting))

		data class RegisteredFilter(val generator: () -> BazaarFilter)
		private val filterKeys = mutableMapOf<String, RegisteredFilter>()
		private val filters = mutableMapOf<KClass<out BazaarFilter>, String>()

		private inline fun <reified T : BazaarFilter> registerFilterType(name: String, noinline generator: () -> T): RegisteredFilter {
			val wrapped = RegisteredFilter(generator)
			filterKeys[name] = wrapped
			filters[T::class] = name
			return wrapped
		}

		init {
			registerFilterType("CITY_BLACKLIST", ::CityBlacklist)
			registerFilterType("CITY_WHITELIST", ::CityWhitelist)
			registerFilterType("PLAYER_BLACKLIST", ::PlayerBlacklist)
			registerFilterType("PLAYER_WHITELIST", ::PlayerWhitelist)
		}

		fun getAllFilters() = filterKeys.toMap()
		fun getKey(filter: BazaarFilter) = filters[filter::class]!!
		fun getFilter(key: String) = filterKeys[key]!!
	}
}
