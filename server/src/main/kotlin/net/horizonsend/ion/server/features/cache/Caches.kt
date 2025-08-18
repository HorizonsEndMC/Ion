package net.horizonsend.ion.server.features.cache

import net.horizonsend.ion.common.database.cache.BookmarkCache
import net.horizonsend.ion.common.database.cache.Cache
import net.horizonsend.ion.common.database.cache.nations.ChestShopCache
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.cache.trade.CargoCrates
import net.horizonsend.ion.server.features.cache.trade.EcoStations
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache

object Caches : IonServerComponent() {
	private val caches: List<Cache> = listOf(
		PlayerCache,
		PlayerSettingsCache,
		SettlementCache,
		NationCache,
		RelationCache,

		CargoCrates,
		EcoStations,
		SpaceStationCache,
		BookmarkCache,
		ChestShopCache
	)

	override fun onEnable() = caches.forEach(Cache::load)
}

