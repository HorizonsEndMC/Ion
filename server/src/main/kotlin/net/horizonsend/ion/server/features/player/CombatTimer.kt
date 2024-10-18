package net.horizonsend.ion.server.features.player

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID
import java.util.concurrent.TimeUnit

object CombatTimer : IonServerComponent() {

	private const val PVP_TIMER_MINS = 10L
	private const val NPC_TIMER_MINS = 5L
	private const val SVP_ENTER_COMBAT_DIST = 500.0
	private const val MAINTAIN_COMBAT_DIST = 1000.0

	var enabled = false

	private val npcTimer = mutableMapOf<UUID, Long>()
	private val pvpTimer = mutableMapOf<UUID, Long>()

	override fun onEnable() {
		enabled = IonServer.configuration.serverName == "Survival"

		if (!enabled) return

		Tasks.syncRepeat(0L, 20L) {

			// Remove combat tags if enough time has elapsed
			for (entry in npcTimer) {
				if (entry.value >= System.currentTimeMillis()) {
					npcTimer.remove(entry.key)
				}
			}

			for (entry in pvpTimer) {
				if (entry.value >= System.currentTimeMillis()) {
					pvpTimer.remove(entry.key)
				}
			}

			Bukkit.getOnlinePlayers().forEach { player ->
				val pilotedStarship = PilotedStarships[player]

				if (pilotedStarship != null) {
					// Piloted ships with active gravity wells will place combat tags on all passengers
					if (pilotedStarship.isInterdicting) {
						for (passenger in pilotedStarship.onlinePassengers) {
							refreshPvpTimer(passenger)
						}
					}

					val starshipCom  = pilotedStarship.centerOfMass.toLocation(player.world)

					// Piloted ships will place combat tags on other players that are not piloting ships within 500 blocks if the pilot is unfriendly to them
					toPlayersInRadius(starshipCom, SVP_ENTER_COMBAT_DIST) { otherPlayer ->
						if (PilotedStarships[otherPlayer] == null) {
							for (passenger in pilotedStarship.onlinePassengers) {
								evaluatePvp(passenger, otherPlayer, neutralAndNoneTriggersCombat = false)
							}
						}
					}

					// Piloted ships will maintain combat tag on all players, within 1000 blocks if the pilot is unfriendly to them
					toPlayersInRadius(starshipCom, MAINTAIN_COMBAT_DIST) { otherPlayer ->
						for (passenger in pilotedStarship.onlinePassengers) {
							evaluatePvp(passenger, otherPlayer, neutralAndNoneTriggersCombat = false)
						}
					}
				}
			}
		}

		// Remove all combat tags on death
		listen<PlayerDeathEvent> { event ->
			npcTimer.remove(event.player.uniqueId)
			pvpTimer.remove(event.player.uniqueId)
		}

		// Apply combat tags in PvP
		listen<EntityDamageByEntityEvent> { event ->
			if (event.damager !is Player || event.entity !is Player) return@listen

			evaluatePvp(event.damager as Player, event.entity as Player)
		}
	}

	fun refreshNpcTimer(player: Player) {
		if (!enabled) return
		npcTimer[player.uniqueId] = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(NPC_TIMER_MINS)
	}

	fun refreshPvpTimer(player: Player) {
		if (!enabled) return
		pvpTimer[player.uniqueId] = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(PVP_TIMER_MINS)
	}

	fun evaluatePvp(attacker: Player, defender: Player, neutralAndNoneTriggersCombat: Boolean = true) {
		if (!enabled) return

		val attackerData = PlayerCache[attacker]
		val attackerNation = attackerData.nationOid

		val defenderData = PlayerCache[defender]
		val defenderNation = defenderData.nationOid

		if (attackerNation == defenderNation) return

		if (neutralAndNoneTriggersCombat) {
			if (attackerNation != null && defenderNation != null &&
				RelationCache[attackerNation, defenderNation] >= NationRelation.Level.FRIENDLY) {
				// Prevent combat tag if both players are in nations and the nations are friendly or better
				return
			}
		} else {
			if (attackerNation == null || defenderNation == null ||
				RelationCache[attackerNation, defenderNation] > NationRelation.Level.UNFRIENDLY) {
				// Prevent combat tag if either player is not in a nation, or the nations have better relations than unfriendly
				return
			}
		}

		// Fell through relation checks; refresh PvP timer
		refreshPvpTimer(attacker)
		refreshPvpTimer(defender)
	}

	fun isNpcCombatTagged(player: Player): Boolean {
		return npcTimer[player.uniqueId] != null
	}

	fun isPvpCombatTagged(player: Player): Boolean {
		return pvpTimer[player.uniqueId] != null
	}
}
