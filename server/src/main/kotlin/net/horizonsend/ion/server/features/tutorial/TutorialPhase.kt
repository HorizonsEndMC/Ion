package net.horizonsend.ion.server.features.tutorial

import net.horizonsend.ion.server.features.tutorial.message.TutorialMessage
import org.bukkit.entity.Player

interface TutorialPhase {
	val messages: Array<out TutorialMessage>
	val cancelEvent: Boolean
	val announceCompletion: Boolean

	fun onStart(player: Player) {}

	fun onEnd(player: Player) {}

	fun setupHandlers()

	val entries: List<TutorialPhase>

	val ordinal: Int
}
