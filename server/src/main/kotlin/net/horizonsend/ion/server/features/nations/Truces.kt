package net.horizonsend.ion.server.features.nations

import net.horizonsend.ion.common.database.cache.nations.TruceCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent

object Truces : IonServerComponent() {
	fun isTruceApplied(playerOne: SLPlayerId, playerTwo: SLPlayerId): Boolean {
		val playerOneCached = PlayerCache[playerOne]
		val playerTwoCached = PlayerCache[playerTwo]

		val isMemberParty = TruceCache[playerOne].any {
			it.partyPlayers.contains(playerTwo)
			|| it.partySettlements.contains(playerTwoCached.settlementOid)
			|| it.victor == playerTwoCached.nationOid
			|| it.defeated == playerTwoCached.nationOid
		}

		if (isMemberParty) return true

		playerOneCached.settlementOid?.let { settlement ->
			val isSettlementParty = TruceCache[settlement].any {
				it.partyPlayers.contains(playerTwo)
					|| it.partySettlements.contains(playerTwoCached.settlementOid)
					|| it.victor == playerTwoCached.nationOid
					|| it.defeated == playerTwoCached.nationOid
			}

			if (isSettlementParty) return true
		}

		playerOneCached.nationOid?.let { nation ->
			val isSettlementParty = TruceCache[nation].any {
				it.partyPlayers.contains(playerTwo)
					|| it.partySettlements.contains(playerTwoCached.settlementOid)
					|| it.victor == playerTwoCached.nationOid
					|| it.defeated == playerTwoCached.nationOid
			}

			if (isSettlementParty) return true
		}

		return false
	}

	@EventHandler
	fun onPlayerHurtPlayer(event: EntityDamageByEntityEvent) {
		val entity = event.entity
		if (entity !is Player) return
		val damager = event.damager
		if (damager !is Player) return

		if (isTruceApplied(entity.slPlayerId, damager.slPlayerId)) event.isCancelled = true
	}
}
