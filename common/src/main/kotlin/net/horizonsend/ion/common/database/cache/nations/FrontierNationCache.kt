package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierTerritory
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.kyori.adventure.text.format.TextColor
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

object FrontierNationCache : ManualCache() {
	data class FrontierNationData(
		val id: Oid<FrontierNation>,
		var name: String,
		var leader: SLPlayerId,
		var color: Int,
		var territory: Oid<FrontierTerritory>,
		var invites: Set<SLPlayerId>,
		var points: Int,
		var siegable: Boolean
	) {
		val textColor: TextColor get() = TextColor.color(color)
	}

	private val FRONTIER_NATION_DATA = ConcurrentHashMap<Oid<FrontierNation>, FrontierNationData>()
	private val nameCache = ConcurrentHashMap<String, Oid<FrontierNation>>()

	override fun load() {
		FRONTIER_NATION_DATA.clear()
		nameCache.clear()

		for (frontierNation in FrontierNation.all()) {
			createCached(frontierNation)
		}

		setupDb(
			FrontierNation.Companion,
			FrontierNation::name,
			FrontierNation::leader,
			FrontierNation::color,
			FrontierNation::territory,
			FrontierNation::invites,
			FrontierNation::points,
			FrontierNation::siegable,
		)
	}

	fun all(): List<FrontierNationData> = FRONTIER_NATION_DATA.values.toList()

	operator fun get(frontierNationId: Oid<FrontierNation>): FrontierNationData = FRONTIER_NATION_DATA[frontierNationId]
		?: error("$frontierNationId wasn't cached")

	fun getByName(name: String): Oid<FrontierNation>? = nameCache[name]

	fun createCached(frontierNation: FrontierNation): FrontierNationData {
		val cachedNation = FrontierNationData(
			frontierNation._id,
			frontierNation.name,
			frontierNation.leader,
			frontierNation.color,
			frontierNation.territory,
			frontierNation.invites,
			frontierNation.points,
			frontierNation.siegable
		)

		FRONTIER_NATION_DATA[frontierNation._id] = cachedNation
		nameCache[frontierNation.name] = cachedNation.id

		return cachedNation
	}

	fun setupDb(
		companion: FrontierNation.Companion,
		nameProperty: KProperty<String>,
		leaderProperty: KProperty<SLPlayerId>,
		colorProperty: KProperty<Int>,
		territoryProperty: KProperty<Oid<FrontierTerritory>>,
		invitesProperty: KProperty<Set<SLPlayerId>>,
		pointsProperty: KProperty<Int>,
		siegableProperty: KProperty<Boolean>,
	) {
		companion.watchInserts { change ->
			change.fullDocument?.let { createCached(it) }
		}

		companion.watchDeletes { change ->
			val id = change.oid

			val data = FRONTIER_NATION_DATA[id] ?: error("$id wasn't cached")

			FRONTIER_NATION_DATA.remove(id)
			nameCache.remove(data.name)
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = FRONTIER_NATION_DATA[id] ?: return@synced

				change[nameProperty]?.let {
					nameCache.remove(data.name)
					data.name = it.string()
					nameCache[data.name] = id
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = FRONTIER_NATION_DATA[id] ?: return@synced

				change[leaderProperty]?.let {
					data.leader = it.slPlayerId()
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = FRONTIER_NATION_DATA[id] ?: return@synced

				change[colorProperty]?.let {
					data.color = it.int()
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = FRONTIER_NATION_DATA[id] ?: return@synced

				change[territoryProperty]?.let {
					data.territory = it.oid()
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = FRONTIER_NATION_DATA[id] ?: return@synced

				change[invitesProperty]?.let {
					data.invites = it.mappedSet { id -> id.slPlayerId() }
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = FRONTIER_NATION_DATA[id] ?: return@synced

				change[pointsProperty]?.let {
					data.points = it.int()
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = FRONTIER_NATION_DATA[id] ?: return@synced

				change[siegableProperty]?.let {
					data.siegable = it.boolean()
				}
			}
		}
	}
}
