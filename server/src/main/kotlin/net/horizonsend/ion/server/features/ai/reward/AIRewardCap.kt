package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.set
import kotlin.math.cbrt

object AIRewardCap : IonServerComponent() {
	private val playerScores: MutableMap<UUID, Int> = mutableMapOf()
	private const val MAX_SCORE = 1000
	private const val PENALTY = 0.5
	const val MAX_CAP_MULTIPLIER = 2
	const val SHIP_SIZE_MULTIPLIER = 4.0


	fun getPenalty(player: Player): Double {
		val score = playerScores[player.uniqueId] ?: 0
		if (score <= MAX_SCORE) {
			return 1.0
		}
		if (score > MAX_SCORE * MAX_CAP_MULTIPLIER) {
			return 0.0
		}
		return PENALTY
	}

	fun addToCap(player: Player, score: Int) {
		val entry = playerScores[player.uniqueId]

		if (entry == null) {
			playerScores[player.uniqueId] = 0
		} else {
			playerScores[player.uniqueId] = score + entry
		}

		val penalty = getPenalty(player)
		if (penalty < 1.0) {
			player.sendMessage(template(
				message = Component.text("Due to overzealous sinking rewards are reduced by {0}% {1}.", NamedTextColor.DARK_RED),
				paramColor = NamedTextColor.RED,
				"%.0f".format((1 - penalty) * 100),
				text("[Details]", HE_LIGHT_BLUE).hoverEvent(text("The cap resets every server restart"))
			))
		}
	}

	fun processDamager(
		starship: Starship,
		damager: PlayerDamager,
		points: AtomicInteger,
		pointsSum: Int
	) {
		val killedSize = starship.initialBlockCount.toDouble()
		val percent = points.get().toDouble() / pointsSum.toDouble()
		val score = (cbrt(killedSize) * SHIP_SIZE_MULTIPLIER * percent).toInt()

		if (score <= 0) return
		addToCap(damager.player, score)
	}
}
