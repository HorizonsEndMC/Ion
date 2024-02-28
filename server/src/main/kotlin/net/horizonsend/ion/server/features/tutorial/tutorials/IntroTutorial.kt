package net.horizonsend.ion.server.features.tutorial.tutorials

import net.horizonsend.ion.common.utils.text.HORIZONS_END
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.tutorial.message.ActionMessage
import net.horizonsend.ion.server.features.tutorial.message.PopupMessage
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent

object IntroTutorial : Tutorial() {
	private val MOVE_TO_BEGIN = registerSimplePhase(
		PopupMessage(
			title = ofChildren(text("Welcome to ", HE_MEDIUM_GRAY), HORIZONS_END),
			subtitle = text("Move to begin a short introductino to the server.", HE_LIGHT_ORANGE)
		),
		cancelEvent = false
	) {
		on<PlayerMoveEvent>({ it.player }) on@{ _, player -> moveToNextStep(player) }
	}

	private val SERVER_INTRO = registerSimplePhase(
		PopupMessage(
			title = text("Horizon's End is a space themed server with moving starships,", HE_LIGHT_GRAY),
			subtitle = text("Nations, a custom economy, factories, and more.", HE_LIGHT_GRAY)
		),
		PopupMessage(
			title = text("There are 3 main servers:", HE_LIGHT_GRAY),
			subtitle = text("Survival, a creative building server, and this hub.", HE_LIGHT_GRAY)
		),
		ActionMessage(
			title = text("We have some features that can be difficult to learn;", HE_LIGHT_GRAY),
			subtitle = text("Would you like to start a tutorial? ", HE_LIGHT_GRAY)
		) { player ->
			moveToNextStep(player)
			//TODO prompts
		},
		cancelEvent = false
	) {}

	override fun startTutorial(player: Player) {
		playerPhases[player.uniqueId] = FlightTutorial.firstPhase
		startPhase(player, FlightTutorial.firstPhase)
	}

	override fun endTutorial(player: Player) {

	}

	override val firstPhase: TutorialPhase = MOVE_TO_BEGIN
	override val lastPhase: TutorialPhase = SERVER_INTRO
}
