package net.horizonsend.ion.server.features.cache

import net.horizonsend.ion.server.features.cache.nations.NationCache
import net.horizonsend.ion.server.features.cache.nations.PlayerCache
import net.horizonsend.ion.server.features.cache.nations.RelationCache
import net.horizonsend.ion.server.features.cache.nations.SettlementCache
import net.horizonsend.ion.server.features.cache.trade.CargoCrates
import net.horizonsend.ion.server.features.cache.trade.EcoStations
import net.starlegacy.SLComponent

object Caches : SLComponent() {
	private val caches: List<Cache> = listOf(
		PlayerCache,
		SettlementCache,
		NationCache,
		RelationCache,

		CargoCrates,
		EcoStations
	)

	override fun onEnable() = caches.forEach(Cache::load)
}
