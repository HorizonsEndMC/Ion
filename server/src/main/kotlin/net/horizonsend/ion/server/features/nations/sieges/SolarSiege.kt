package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeData
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.types.RegionSolarSiegeZone
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

class SolarSiege(
	val databaseId: Oid<SolarSiegeData>,
	val region: RegionSolarSiegeZone,
	val attacker: Oid<Nation>,
	attackerPoints: Int = 0,
	val defender: Oid<Nation>,
	defenderPoints: Int = 0,
) {
	var needsSave: Boolean = false; private set

	var attackerPoints: Int = attackerPoints
		@Synchronized get
		@Synchronized set(value) {
			field = value
			needsSave = true
		}

	var defenderPoints: Int = defenderPoints
		@Synchronized get
		@Synchronized set(value) {
			field = value
			needsSave = true
		}

	fun saveSiegeData() = SolarSiegeData.updatePoints(databaseId, attackerPoints, defenderPoints)

	fun formatName(): Component {
		return template(text("{0}'s siege of {1}", HE_MEDIUM_GRAY), formatNationName(attacker), region.world)
	}

	fun isAttacker(player: SLPlayerId): Boolean {
		val playerNation = PlayerCache[player].nationOid ?: return false

		return RelationCache[attacker, playerNation] >= NationRelation.Level.ALLY
	}

	fun isDefender(player: SLPlayerId): Boolean {
		val playerNation = PlayerCache[player].nationOid ?: return false

		return RelationCache[defender, playerNation] >= NationRelation.Level.ALLY
	}
}
