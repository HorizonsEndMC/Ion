package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import java.util.UUID
import kotlin.math.floor

object AIKillStreak : IonServerComponent() {
	private val playerHeatList: MutableList<PlayerHeat> = mutableListOf()
	private const val BASE_DECAY = 5
	private const val ADDITIONAL_DECAY_PER_LEVEL = 0.2
	const val MAXLVLS = 60
	const val MAXMULTIPLIER = 6.0

	override fun onEnable() {
		Tasks.syncRepeat(200, 20) {
			tick()
		}
	}

	private fun tick() {
		decayHeat()
	}

	private fun decayHeat() {
		val invalid = mutableListOf<PlayerHeat>()
		playerHeatList.forEach {
			val player = it.getPlayer()

			if (player == null) {
				invalid.add(it)
				return@forEach
			}

			val decayRate = BASE_DECAY + floor(ADDITIONAL_DECAY_PER_LEVEL * it.currentHeat).toInt()
			val decayAmount = if (CombatTimer.isNpcCombatTagged(player)) decayRate else decayRate * 3

			it.score = (it.score - decayAmount).coerceAtLeast(0)
			if (calculateHeat(it.score) < it.currentHeat) {
				it.currentHeat = calculateHeat(it.score)
				it.getPlayer()?.sendMessage(
					template(
						message = Component.text("Reduced AI heat streak to {0}", NamedTextColor.GRAY),
						it.currentHeat
					)
				)
			}
		}

		playerHeatList.removeAll(invalid)
		playerHeatList.removeIf { it.score <= 0 }
	}

	fun rewardHeat(player: Player, score: Int) {
		var entry = playerHeatList.find { it.playerId == player }
		if (entry == null) {
			entry = PlayerHeat(player.uniqueId, score, 0)
			playerHeatList.add(entry)
		} else {
			entry.score += score
		}
		if (calculateHeat(entry.score) > entry.currentHeat) {
			entry.currentHeat = calculateHeat(entry.score)
			entry.getPlayer()?.sendMessage(
				template(
					message = Component.text("Increased AI heat streak to {0}, rewards increased by {1}%", NamedTextColor.GOLD),
					entry.currentHeat,
					"%.0f".format(getHeatMultiplier(player) * 100 - 100)
				)
			)
		}
	}

	private fun calculateHeat(score: Int): Int {
		return ((score + 500) / 1000).coerceAtMost(MAXLVLS)
	}

	fun getHeatMultiplier(player: Player): Double {
		val streak = playerHeatList.find { it.playerId == player }?.currentHeat ?: 0
		return (1.0 + streak.toDouble() * 0.1).coerceAtMost(MAXMULTIPLIER)
	}


	@EventHandler
	fun onShipSink(event: StarshipSunkEvent) {
		val pilot = event.starship.playerPilot?.uniqueId ?: return
		val match = playerHeatList.firstOrNull { it.playerId == pilot } ?: return
		val player = match.getPlayer() ?: return

		match.score /= 2
		val currentHeat = match.currentHeat
		match.currentHeat = calculateHeat(score = match.score)
		if (match.currentHeat < currentHeat) {

			player.sendMessage(
				template(
					message = Component.text("Due to sinking heat has been reduced by half. current streak: {0}, rewards decreased to {1}%", NamedTextColor.RED),
					match.currentHeat,
					"%.0f".format(getHeatMultiplier(player)*100-100)
				)
			)
		}
	}

	data class PlayerHeat(
		val playerId: UUID,
		var score: Int,
		var currentHeat: Int
	) {
		fun getPlayer(): Player? = Bukkit.getPlayer(playerId)
	}
}


