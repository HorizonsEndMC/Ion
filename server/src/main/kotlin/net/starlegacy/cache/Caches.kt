package net.starlegacy.cache

import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.RelationCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.cache.trade.CargoCrates
import net.starlegacy.cache.trade.EcoStations

object Caches : SLComponent() {
	private val caches: List<Cache> = listOf(
		PlayerCache,
		SettlementCache,
		RelationCache,

		CargoCrates,
		EcoStations
	)

	override fun onEnable() = caches.forEach(Cache::load)

	override fun supportsVanilla(): Boolean {
		return true
	}
}
