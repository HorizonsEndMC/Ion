package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.array
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.DominionTerritory
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.space.GalacticMap
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class RegionDominionTerritory(territory: DominionTerritory) :
	Region<DominionTerritory>(territory),
	RegionTopLevel,
	RegionParent {
	override val priority: Int = 0
	override var world: String = territory.world; private set
	var nation: Oid<Nation>? = territory.nation; private set
	var alias: String? = territory.alias; private set
	var name: String = territory.name; private set
	var trustedNations: Set<Oid<Nation>> = territory.trustedNations; private set
	var trustedSettlements: Set<Oid<Settlement>> = territory.trustedSettlements; private set
	var trustedPlayers: Set<SLPlayerId> = territory.trustedPlayers; private set

	override var children: MutableSet<Region<*>> = ConcurrentHashMap.newKeySet()

	override fun contains(x: Int, y: Int, z: Int): Boolean = true

	override fun update(delta: ChangeStreamDocument<DominionTerritory>) {
		delta[DominionTerritory::world]?.let { world = it.string() }
		delta[DominionTerritory::name]?.let { name = it.string() }
		delta[DominionTerritory::nation]?.let { nation = it.nullable()?.oid() }
		delta[DominionTerritory::alias]?.let { alias = it.string() }
		delta[DominionTerritory::trustedNations]?.let { bson ->
			trustedNations = bson.array().mappedSet { it.oid() }
		}
		delta[DominionTerritory::trustedSettlements]?.let { bson ->
			trustedSettlements = bson.array().mappedSet { it.oid() }
		}
		delta[DominionTerritory::trustedPlayers]?.let { bson ->
			trustedPlayers = bson.array().mappedSet { it.slPlayerId() }
		}

		NationsMap.updateDominionTerritory(this)
		GalacticMap.updateGalacticIconByName(name)
	}

	val isUnclaimed get() = nation == null
	val isClaimed get() = nation != null

	override fun calculateInaccessMessage(player: Player): String? {
		if (trustedPlayers.contains(PlayerCache[player].id)) return null
		if (trustedSettlements.contains(PlayerCache[player].settlementOid)) return null
		if (trustedNations.contains(PlayerCache[player].nationOid)) return null

		val playerNation: Oid<Nation>? = PlayerCache[player].nationOid
		val nation = nation ?: return null
		if (playerNation == nation) return null
		return "$world is claimed by ${NationCache[nation].name}"
	}

	override fun toString(): String = "$name ($world)"
}
