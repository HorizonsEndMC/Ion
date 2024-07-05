package net.horizonsend.ion.proxy.features.cache

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.cache.Cache
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.proxy.PLUGIN

object Caches : IonComponent() {
	private val caches: List<Cache> = listOf(
		PlayerCache,
		SettlementCache,
		NationCache,
		RelationCache
	)

	override fun onEnable() = caches.forEach {
		it.load()

		if (it is Listener) PLUGIN.server.eventManager.register(PLUGIN, it)
	}
}
