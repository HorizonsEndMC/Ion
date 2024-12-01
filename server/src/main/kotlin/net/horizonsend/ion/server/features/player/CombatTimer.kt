package net.horizonsend.ion.server.features.player

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.UnpilotedController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.TextDecoration.BOLD
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.Duration
import java.util.UUID

object CombatTimer : IonServerComponent() {
	private val PVP_TIMER_MINS = Duration.ofMinutes(5)
	private val NPC_TIMER_MINS = Duration.ofMinutes(2).plusSeconds(30)
	private const val SVP_ENTER_COMBAT_DIST = 500.0
	private const val MAINTAIN_COMBAT_DIST = 1000.0
	private const val REASON_NPC_SVS_COMBAT = "Engaging in combat with an NPC starship"
	private const val REASON_PVP_SVS_COMBAT = "Engaging in combat with another player's starship"
	private const val REASON_PVP_WITHIN_GRAVITY_WELL = "Getting caught in non-friendly starship's gravity well"
	const val REASON_PVP_GROUND_COMBAT = "Engaging in combat with another player on the ground"
	private const val REASON_ENEMY_PROXIMITY = "Being in close proximity to a hostile starship"

	private var enabled = false

	private val npcTimer = mutableMapOf<UUID, Long>()
	private val pvpTimer = mutableMapOf<UUID, Long>()

	override fun onEnable() {
		enabled = ConfigurationFiles.featureFlags().combatTimers

		if (!enabled) return

		Tasks.syncRepeat(0L, 20L) {

			// Remove combat tags if enough time has elapsed
			for (entry in npcTimer) {
				if (entry.value <= System.currentTimeMillis()) {
					npcTimer.remove(entry.key)
					Bukkit.getPlayer(entry.key)?.success("You are no longer in combat (NPC)")
				}
			}

			for (entry in pvpTimer) {
				if (entry.value <= System.currentTimeMillis()) {
					pvpTimer.remove(entry.key)
					Bukkit.getPlayer(entry.key)?.success("You are no longer in combat (PvP)")
				}
			}

			Bukkit.getOnlinePlayers().forEach { player ->
				val pilotedStarship = PilotedStarships[player]

				// Only actively controlled warships can cause proximity triggered combat tags
				if (pilotedStarship != null && pilotedStarship.controller !is UnpilotedController &&
					pilotedStarship.type.typeCategory == TypeCategory.WAR_SHIP) {
					val starshipCom  = pilotedStarship.centerOfMass.toLocation(player.world)

					if (pilotedStarship.isInterdicting && pilotedStarship.world.hasFlag(WorldFlag.SPACE_WORLD)) {
						// Interdicting ships will place combat tags on other player starships that are within the well range, are less than neutral, and not in a protected city
						toPlayersInRadius(starshipCom, Interdiction.starshipInterdictionRangeEquation(pilotedStarship)) { otherPlayer ->
							val otherStarship = PilotedStarships[otherPlayer]
							if (otherStarship != null &&
								!ProtectionListener.isProtectedCity(otherStarship.centerOfMass.toLocation(otherPlayer.world))) {
								evaluatePvp(
									player,
									otherPlayer,
									REASON_PVP_WITHIN_GRAVITY_WELL,
									neutralTriggersCombat = false
								)
							}
						}
					}

					// Piloted ships will place combat tags on other players that are unfriendly if they are within 500 blocks, the defender is not piloting a ship, and they are not in a protected city
					toPlayersInRadius(starshipCom, SVP_ENTER_COMBAT_DIST) { otherPlayer ->
						if (PilotedStarships[otherPlayer] == null &&
							!ProtectionListener.isProtectedCity(otherPlayer.location)) {
							evaluatePvp(
								player,
								otherPlayer,
								REASON_ENEMY_PROXIMITY,
								neutralTriggersCombat = false,
								tagAttacker = false
							)
						}
					}

					// Piloted ships will maintain combat tag on all players, within 1000 blocks if the pilot is unfriendly to them and the other player was already tagged and not in a protected city
					toPlayersInRadius(starshipCom, MAINTAIN_COMBAT_DIST) { otherPlayer ->
						if (isPvpCombatTagged(otherPlayer) &&
							!ProtectionListener.isProtectedCity(otherPlayer.location)) {
							evaluatePvp(
								player,
								otherPlayer,
								REASON_ENEMY_PROXIMITY,
								neutralTriggersCombat = false,
								tagAttacker = false
							)
						}
					}
				}
			}
		}

		// Remove all combat tags on death
		listen<PlayerDeathEvent> { event ->
			if (npcTimer[event.player.uniqueId] != null) {
				event.player.success("You are no longer in combat (NPC)")
				npcTimer.remove(event.player.uniqueId)
			}
			if (pvpTimer[event.player.uniqueId] != null) {
				event.player.success("You are no longer in combat (PVP)")
				pvpTimer.remove(event.player.uniqueId)
			}
		}
	}


	/**
	 * Applies or refreshes an NPC combat tag to a player.
	 */
	fun refreshNpcTimer(player: Player, reason: String) {
		if (!enabled) return

		if (!isNpcCombatTagged(player) && PlayerCache[player].enableCombatTimerAlerts) {
			player.alert("You are now in combat (NPC)")
			player.sendMessage(npcTimerAlertComponent(reason))
			if (player.hasProtection() && !player.world.hasFlag(WorldFlag.NOT_SECURE)) {
				player.sendMessage(newPlayerAlertComponent())
			}
		}

		npcTimer[player.uniqueId] = System.currentTimeMillis() + NPC_TIMER_MINS.toMillis()
	}

	/**
	 * Applies or refreshes a PvP combat tag to a player.
	 */
	fun refreshPvpTimer(player: Player, reason: String) {
		if (!enabled) return

		if (!isPvpCombatTagged(player) && PlayerCache[player].enableCombatTimerAlerts) {
			player.alert("You are now in combat (PVP)")
			player.sendMessage(pvpTimerAlertComponent(reason))
			if (player.hasProtection() && !player.world.hasFlag(WorldFlag.NOT_SECURE)) {
				player.sendMessage(newPlayerAlertComponent())
			}
		}

		pvpTimer[player.uniqueId] = System.currentTimeMillis() + PVP_TIMER_MINS.toMillis()
	}

	/**
	 * Checks if PvP combat tags should be applied to two players in an interaction
	 * @param neutralTriggersCombat if combat tags should be applied if the player relations are NEUTRAL or lower
	 * (default is NONE or lower)
	 * @param tagAttacker if combat tags should not be applied to the attacker
	 */
	fun evaluatePvp(attacker: Player, defender: Player, reason: String, neutralTriggersCombat: Boolean = true, tagAttacker: Boolean = true) {
		if (!enabled) return
		if (attacker == defender) return

		if (attacker.hasPermission("group.dutymode") || defender.hasPermission("group.dutymode")) return

		// don't run for combat NPCs
		if (defender.hasMetadata("NPC")) return

		val attackerData = PlayerCache[attacker]
		val attackerNation = attackerData.nationOid

		val defenderData = PlayerCache[defender]
		val defenderNation = defenderData.nationOid

		if (attackerNation == defenderNation) return

		if (neutralTriggersCombat) {
			if (attackerNation != null && defenderNation != null &&
				RelationCache[attackerNation, defenderNation] >= NationRelation.Level.FRIENDLY) {
				// Prevent combat tag if both players are in nations and the nations are friendly or better
				// Primarily for direct attack trigger
				return
			}
		} else {
			if ((attackerNation != null && defenderNation != null &&
				RelationCache[attackerNation, defenderNation] >= NationRelation.Level.NEUTRAL) ||
				defender.hasProtection()) {
				// Prevent combat tag if relation is NEUTRAL or better, OR the defender has noob protection
				// Primarily for proximity trigger
				return
			}
		}

		// Fell through relation checks; refresh PvP timer
		if (tagAttacker) refreshPvpTimer(attacker, reason)
		refreshPvpTimer(defender, reason)
	}

	/**
	 * Checks if PvP combat tags should be applied to two starships in an interaction
	 */
	fun evaluateSvs(shooter: Damager, defendingStarship: ActiveStarship) {
		if (!enabled) return
		if (shooter is PlayerDamager && shooter.player.hasPermission("group.dutymode")) return
		if (defendingStarship.playerPilot?.hasPermission("group.dutymode") == true) return

		if (shooter is AIShipDamager && defendingStarship.controller is PlayerController) {
			// refresh NPC combat timer if attacker is AI and defender is player
			refreshNpcTimer((defendingStarship.controller as PlayerController).player, REASON_NPC_SVS_COMBAT)
		} else if (shooter is PlayerDamager) {
			if (defendingStarship.controller is PlayerController) {
				// evaluate PVP timer if both attacker and defender are players
				evaluatePvp(shooter.player, (defendingStarship.controller as PlayerController).player, REASON_PVP_SVS_COMBAT)
			} else if (defendingStarship.controller is AIController) {
				// refresh NPC combat timer if attacker is player and defender is AI
				refreshNpcTimer(shooter.player, REASON_NPC_SVS_COMBAT)
			}
		}
	}

	/**
	 * Checks if the player is NPC combat tagged
	 */
	fun isNpcCombatTagged(player: Player): Boolean {
		return npcTimer[player.uniqueId] != null
	}

	/**
	 * Checks if the player is PvP combat tagged
	 */
	fun isPvpCombatTagged(player: Player): Boolean {
		return pvpTimer[player.uniqueId] != null
	}

	/**
	 * Adds or refreshes an NPC combat tag to a player UUID
	 */
	fun addNpcCombatTag(uuid: UUID) {
		npcTimer[uuid] = System.currentTimeMillis() + NPC_TIMER_MINS.toMillis()
	}

	/**
	 * Adds or refreshes a PvP combat tag to a player UUID
	 */
	fun addPvpCombatTag(uuid: UUID) {
		pvpTimer[uuid] = System.currentTimeMillis() + PVP_TIMER_MINS.toMillis()
	}

	/**
	 * Removes an NPC combat tag from a player UUID
	 */
	fun removeNpcCombatTag(uuid: UUID) {
		npcTimer.remove(uuid)
	}

	/**
	 * Removes a PvP combat tag from a player UUID
	 */
	fun removePvpCombatTag(uuid: UUID) {
		pvpTimer.remove(uuid)
	}

	/**
	 * Returns the amount of time remaining on a player's NPC combat tag in milliseconds
	 */
	fun npcTimerRemainingMillis(player: Player): Long {
		val endTime = npcTimer[player.uniqueId]
		return if (endTime == null) {
			0L
		} else {
			(endTime - System.currentTimeMillis()).coerceAtLeast(0L)
		}
	}

	/**
	 * Returns the amount of time remaining on a player's PvP combat tag in milliseconds
	 */
	fun pvpTimerRemainingMillis(player: Player): Long {
		val endTime = pvpTimer[player.uniqueId]
		return if (endTime == null) {
			0L
		} else {
			(endTime - System.currentTimeMillis()).coerceAtLeast(0L)
		}
	}

	/**
	 * Constructor for the alert message received when obtaining an NPC combat tag
	 */
	private fun npcTimerAlertComponent(reason: String): Component {
		return template(text("""
			{0}
			{1}
			Reason: {2}
			Expiry: {3}
			Consequences: {4}
			{5}
		""".trimIndent(), HE_MEDIUM_GRAY),
			lineBreak(45),
			text(repeatString(" ", 8) + "YOU ARE NOW IN COMBAT (NPC)", GOLD).decorate(BOLD),
			text(reason, HE_LIGHT_BLUE),
			text("${NPC_TIMER_MINS.toMinutesPart()}m ${NPC_TIMER_MINS.toSecondsPart().toString().padStart(2, '0')}s", GOLD),
			text("[Hover]", HE_LIGHT_BLUE).hoverEvent(ofChildren(
				text("- You cannot release your ship", HE_LIGHT_BLUE),
				newline(),
				text("- You cannot claim territories, create settlements, or create space stations", HE_LIGHT_BLUE),
				newline(),
				text("- You cannot kill yourself", HE_LIGHT_BLUE),
			)),
			lineBreak(45),
		)
	}

	/**
	 * Constructor for the alert message received when obtaining a PvP combat tag
	 */
	private fun pvpTimerAlertComponent(reason: String): Component {
		return ofChildren(
			lineBreak(45),
			newline(),
			text(repeatString(" ", 8) + "YOU ARE NOW IN COMBAT (PVP)", DARK_RED).decorate(BOLD),
			newline(),
			text("Reason: ", HE_MEDIUM_GRAY),
			text(reason, HE_LIGHT_BLUE),
			newline(),
			text("Expiry: ", HE_MEDIUM_GRAY),
			text("${PVP_TIMER_MINS.toMinutesPart()}m ${PVP_TIMER_MINS.toSecondsPart().toString().padStart(2, '0')}s", GOLD),
			newline(),
			text("Consequences: ", HE_MEDIUM_GRAY),
			text("[Hover]", HE_LIGHT_BLUE)
				.hoverEvent(ofChildren(
					text("- You cannot release your ship", HE_LIGHT_BLUE),
					newline(),
					text("- You cannot claim territories, create settlements, or create space stations", HE_LIGHT_BLUE),
					newline(),
					text("- You cannot kill yourself", HE_LIGHT_BLUE),
					newline(),
					text("- Any warship that you are piloting cannot enter safe zones", HE_LIGHT_BLUE),
					newline(),
					text("- You or your Combat NPC can be killed within safe zones", HE_LIGHT_BLUE),
					newline(),
					text("- Combat NPCs created when you log off will last for the duration of your combat tag", HE_LIGHT_BLUE),
					newline(),
					text("- Cannot use Power Drill, Drill, Mining Laser, Decomposer, or Ship Factory", HE_LIGHT_BLUE),
					newline(),
					text("- Remaining within ${MAINTAIN_COMBAT_DIST.toInt()} blocks of unfriendly and enemy starships will refresh your combat tag", HE_LIGHT_BLUE),
					)),
			newline(),
			lineBreak(45),
		)
	}

	private fun newPlayerAlertComponent(): Component {
		return ofChildren(
			text(repeatString(" ", 8) + "YOU HAVE NEW PLAYER PROTECTION", GOLD).decorate(BOLD),
			newline(),
			text("You are immune to most forms of damage", HE_LIGHT_BLUE),
			newline(),
			text("Consequences: ", HE_MEDIUM_GRAY),
			text("[Hover]", HE_LIGHT_BLUE)
				.hoverEvent(ofChildren(
					text("- You will not take damage from other ships", HE_LIGHT_BLUE),
					newline(),
					text("- You are protected by new player rules", HE_LIGHT_BLUE),
					newline(),
					text("- Players are forbidden from attacking you unprovoked", HE_LIGHT_BLUE),
					newline(),
					text("- Items/Ship will be returned if you die", HE_LIGHT_BLUE),
					newline(),
					text("- You or your Combat NPC can be killed within safe zones", HE_LIGHT_BLUE),
					newline(),
					text("- Moderation will rule in favor of you", HE_LIGHT_BLUE),
				)),
			newline(),
			text(repeatString(" ", 8) + "DO NOT ATTACK. ATTACKING WILL CANCEL YOUR PROTECTION", DARK_RED).decorate(BOLD),
			newline(),
			lineBreak(45),
		)
	}
}
