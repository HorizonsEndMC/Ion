package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

object AIKillStreak : IonServerComponent() {

	private val playerHeatList: MutableList<PlayerHeat> = mutableListOf()
	private val decay = 5

	override fun onEnable() {
		Tasks.syncRepeat(200, 20) {
			tick()
		}
	}

	private fun tick() {
		decayHeat()
	}

	private fun decayHeat() {
		playerHeatList.forEach {
			it.score = (it.score - decay).coerceAtLeast(0)
			if (calculateHeat(it.score) < it.currentHeat) {
				it.currentHeat = calculateHeat(it.score)
				it.player.sendMessage(
					template(
						message = Component.text("Reduced AI heat streak to {0}", NamedTextColor.GRAY),
						it.currentHeat
					)
				)
			}
		}
		playerHeatList.removeIf { it.score <= 0 }
	}

	fun rewardHeat(player: Player, score: Int) {
		return

		var entry = playerHeatList.find { it.player == player }
		if (entry == null) {
			entry = PlayerHeat(player, score, 0)
			playerHeatList.add(entry)
		} else {
			entry.score += score
		}
		if (calculateHeat(entry.score) > entry.currentHeat) {
			entry.currentHeat = calculateHeat(entry.score)
			entry.player.sendMessage(
				template(
					message = Component.text("Increased AI heat streak to {0}, rewards increased by {1}%", NamedTextColor.GOLD),
					entry.currentHeat,
					entry.currentHeat * 10
				)
			)
		}
	}

	private fun calculateHeat(score: Int): Int {
		return (score + 500) / 1000
	}

	fun getHeatMultiplier(player: Player): Double {
		return 1.0

		val streak = playerHeatList.find { it.player == player }?.currentHeat ?: 0
		return 1.0 + streak.toDouble() * 0.1
	}

	data class PlayerHeat(
		val player: Player,
		var score: Int,
		var currentHeat: Int
	)
}


