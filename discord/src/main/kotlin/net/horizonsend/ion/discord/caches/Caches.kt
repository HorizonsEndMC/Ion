package net.horizonsend.ion.discord.caches

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.cache.BountyCache
import net.horizonsend.ion.common.database.cache.Cache
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache

object Caches : IonComponent() {
	private val caches: List<Cache> = listOf(
//		PlayerCache,
		SettlementCache,
		NationCache,
		RelationCache,
		BountyCache
	)

	override fun onEnable() = caches.forEach(Cache::load)
}
