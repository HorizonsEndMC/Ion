package net.horizonsend.ion.server.features.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.DominionTerritory
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionDominionTerritory
import org.bukkit.entity.Player

object DominionTerritoryBuffTypes {
	// 1 territory buffs
	val WARMUP_1 = DominionTerritoryBuff(requiredTerritories = 1, value = 1.0)       // 1s faster warmup
	val CRATE_BONUS_1 = DominionTerritoryBuff(requiredTerritories = 1, value = 0.10)  // 10% more crate reward
	val CONTACT_RANGE = DominionTerritoryBuff(requiredTerritories = 1, value = 250.0) // 250m extra contacts range
	val SPEED = DominionTerritoryBuff(requiredTerritories = 1, value = 1.0)             // 1 more BPS
	val ACCELERATION = DominionTerritoryBuff(requiredTerritories = 1, value = 0.5)    // 0.5 more accel

	// 3 territory buffs
	val WARMUP_3 = DominionTerritoryBuff(requiredTerritories = 3, value = 0.20)       // 20% warmup buff
	val CRATE_BONUS_3 = DominionTerritoryBuff(requiredTerritories = 3, value = 0.20)  // 20% more crate reward

	val dailyGateTaxCollected = mutableMapOf<Oid<Nation>, Double>()
	val dailyGateTaxResetTime = mutableMapOf<Oid<Nation>, Long>()
	const val GATE_TAX_DAILY_CAP = 20_000.0
	const val GATE_TAX_AMOUNT = 100.0

	fun getTerritoryCount(player: Player): Int {
		val nationId = PlayerCache[player].nationOid ?: return 0
		return Regions.getAllOf<RegionDominionTerritory>().count { it.nation == nationId }
	}

	fun isEffectActive(player: Player, buff: DominionTerritoryBuff): Boolean {
		return getTerritoryCount(player) >= buff.requiredTerritories
	}

	fun getCrateBonus(player: Player): Double {
		val count = getTerritoryCount(player)
		return when {
			count >= 3 -> CRATE_BONUS_3.value
			count >= 1 -> CRATE_BONUS_1.value
			else -> 0.0
		}
	}

	fun getWarmupReduction(player: Player): Double {
		val count = getTerritoryCount(player)
		return when {
			count >= 3 -> WARMUP_3.value  // 20% reduction
			count >= 1 -> WARMUP_1.value  // flat 1s reduction
			else -> 0.0
		}
	}
}

data class DominionTerritoryBuff(
	val requiredTerritories: Int,
	val value: Double
)
