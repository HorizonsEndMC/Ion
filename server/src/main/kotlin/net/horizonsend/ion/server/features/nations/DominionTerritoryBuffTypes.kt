package net.horizonsend.ion.server.features.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionDominionTerritory
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMovement
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

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

	fun doDominionTerritoryBeaconTax(
		starship: ActiveStarship,
		movement: HyperspaceMovement
	) {
		// Gate tax check
		val player = starship.playerPilot ?: return
		val destinationWorld = movement.dest.world

		if (destinationWorld.hasFlag(WorldFlag.DOMINION_WORLD)) {
			val dominionTerritory = Regions.getAllOf<RegionDominionTerritory>()
				.firstOrNull { it.world == destinationWorld.name }
			val ownerNationId = dominionTerritory?.nation

			if (ownerNationId != null) {
				val ownerTerritoryCount = Regions.getAllOf<RegionDominionTerritory>()
					.count { it.nation == ownerNationId }

				if (ownerTerritoryCount >= 5) {
					if (!VAULT_ECO.has(player, GATE_TAX_AMOUNT)) {
						player.userError("You cannot afford the ${GATE_TAX_AMOUNT.toCreditsString()} gate tax to enter ${destinationWorld.name}!")
						return
					}

					// Reset daily cap if 24 hours have passed
					val lastReset = dailyGateTaxResetTime[ownerNationId] ?: 0L
					if (System.currentTimeMillis() - lastReset > TimeUnit.DAYS.toMillis(1)) {
						dailyGateTaxCollected[ownerNationId] = 0.0
						dailyGateTaxResetTime[ownerNationId] = System.currentTimeMillis()
					}

					val currentCollected = dailyGateTaxCollected[ownerNationId] ?: 0.0

					if (currentCollected < GATE_TAX_DAILY_CAP) {
						val actualTax = minOf(GATE_TAX_AMOUNT, GATE_TAX_DAILY_CAP - currentCollected)
						VAULT_ECO.withdrawPlayer(player, actualTax)
						Nation.deposit(ownerNationId, actualTax.toInt())
						dailyGateTaxCollected[ownerNationId] = currentCollected + actualTax
						player.information("Paid ${actualTax.toCreditsString()} gate tax to enter ${destinationWorld.name}.")
					} else {
						// Cap reached, no tax but still allow jump
						player.information("Gate tax cap reached for ${destinationWorld.name}, no tax charged.")
					}
				}
			}
		}
	}
}

data class DominionTerritoryBuff(
	val requiredTerritories: Int,
	val value: Double
)
