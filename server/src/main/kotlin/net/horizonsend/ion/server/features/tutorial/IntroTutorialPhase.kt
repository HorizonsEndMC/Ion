package net.horizonsend.ion.server.features.tutorial

import net.horizonsend.ion.common.utils.text.HORIZONS_END
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.tutorial.message.PopupMessage
import net.horizonsend.ion.server.features.tutorial.message.TutorialMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent

enum class IntroTutorialPhase(
	override vararg val messages: TutorialMessage,
	override val cancelEvent: Boolean = true,
	override val announceCompletion: Boolean = false,
) : TutorialPhase {
	WAIT_TO_MOVE(
		PopupMessage(Component.text("Welcome!", NamedTextColor.WHITE), ofChildren(Component.text("Welcome to ", HEColorScheme.HE_MEDIUM_GRAY), HORIZONS_END)),
		cancelEvent = false
	) {
		override fun setupHandlers() = on<PlayerMoveEvent>({ it.player }) { _, player ->
			moveToNextStep(player)
		}
	},

//	EXPLAIN_TUTORIAL(
//
//	) {
//
//	},

	;

	override val entries: List<TutorialPhase> get() = values().toList()

	companion object : TutorialCompanion() {
		override val WORLD_NAME: String = "Hub"

		override val entries: List<TutorialPhase> get() = values().toList()

		override fun teleportToStart(player: Player) {
			return
		}

		override fun teleportToEnd(player: Player) {
			return
		}

		override fun startTutorial(player: Player) {
			playersInTutorials[player] = FIRST

			return
		}
	}
}
