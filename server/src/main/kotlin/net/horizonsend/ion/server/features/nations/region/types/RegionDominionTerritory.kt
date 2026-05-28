package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.DominionTerritory
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.server.features.cache.PlayerCache
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

	override var children: MutableSet<Region<*>> = ConcurrentHashMap.newKeySet()

	override fun contains(x: Int, y: Int, z: Int): Boolean = true

	override fun update(delta: ChangeStreamDocument<DominionTerritory>) {
		delta[DominionTerritory::world]?.let { world = it.string() }
		delta[DominionTerritory::name]?.let { name = it.string() }
		delta[DominionTerritory::nation]?.let { nation = it.nullable()?.oid() }
		delta[DominionTerritory::alias]?.let { alias = it.string() }
	}

	val isUnclaimed get() = nation == null
	val isClaimed get() = nation != null

	override fun calculateInaccessMessage(player: Player): String? {
		val playerNation: Oid<Nation>? = PlayerCache[player].nationOid
		val nation = nation ?: return null
		if (playerNation == nation) return null
		return "$world is claimed by ${NationCache[nation].name}"
	}

	override fun toString(): String = "$name ($world)"
}
