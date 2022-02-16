package net.starlegacy.feature.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.RelationCache
import net.starlegacy.database.Oid
import net.starlegacy.database.enumValue
import net.starlegacy.database.get
import net.starlegacy.database.int
import net.starlegacy.database.mappedSet
import net.starlegacy.database.oid
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.schema.nations.SpaceStation
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.string
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.util.d
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.squared
import org.bukkit.entity.Player

class RegionSpaceStation(spaceStation: SpaceStation) : Region<SpaceStation>(spaceStation), RegionTopLevel {
	override val priority: Int = 0

	override var world: String = spaceStation.world; private set

	var name: String = spaceStation.name; private set
	var x: Int = spaceStation.x; private set
	var z: Int = spaceStation.z; private set
	var radius: Int = spaceStation.radius; private set
	var nation: Oid<Nation> = spaceStation.nation; private set
	var trustLevel: SpaceStation.TrustLevel = spaceStation.trustLevel; private set
	var managers: Set<SLPlayerId> = spaceStation.managers; private set
	var trustedPlayers: Set<SLPlayerId> = spaceStation.trustedPlayers; private set
	var trustedNations: Set<Oid<Nation>> = spaceStation.trustedNations; private set

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		return distanceSquared(this.x.d(), 0.0, this.z.d(), x.d(), 0.0, z.d()) <= radius.toDouble().squared()
	}

	override fun onCreate() {
		NationsMap.addSpaceStation(this)
	}

	override fun update(delta: ChangeStreamDocument<SpaceStation>) {
		delta[SpaceStation::name]?.let { name = it.string() }
		delta[SpaceStation::world]?.let { world = it.string() }
		delta[SpaceStation::x]?.let { x = it.int() }
		delta[SpaceStation::z]?.let { z = it.int() }
		delta[SpaceStation::radius]?.let { radius = it.int() }
		delta[SpaceStation::nation]?.let { nation = it.oid() }
		delta[SpaceStation::trustLevel]?.let { trustLevel = it.enumValue() }
		delta[SpaceStation::managers]?.let { col -> managers = col.mappedSet { it.slPlayerId() } }
		delta[SpaceStation::trustedPlayers]?.let { col -> trustedPlayers = col.mappedSet { it.slPlayerId() } }
		delta[SpaceStation::trustedNations]?.let { col -> trustedNations = col.mappedSet { it.oid<Nation>() } }

		NationsMap.updateSpaceStation(this)
	}

	override fun onDelete() {
		NationsMap.removeSpaceStation(this)
	}

	override fun calculateInaccessMessage(player: Player): String? {
		val playerData = PlayerCache[player]

		val playerNation: Oid<Nation>? = playerData.nation

		// if they're in a nation, check for trust level auto perms, and trusted nations
		if (playerNation != null) {
			// if trust level is nation and they're in the same nation
			if (trustLevel == SpaceStation.TrustLevel.NATION && nation == playerNation) {
				return null
			}

			// if they're at least an ally and trust level is ally (should cover same nation)
			if (trustLevel == SpaceStation.TrustLevel.ALLY && RelationCache[playerNation, nation] >= NationRelation.Level.ALLY) {
				return null
			}

			// if they're in a trusted nation
			if (trustedNations.contains(playerNation)) {
				return null
			}
		}

		// if they're a manager they can build
		if (managers.contains(playerData.id)) {
			return null
		}

		if (trustedPlayers.contains(playerData.id)) {
			return null
		}

		return "&cSpace station $name is claimed by ${NationCache[nation].name} @ $x,$z x $radius"
	}
}
