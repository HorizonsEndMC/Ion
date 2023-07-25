package net.horizonsend.ion.server.features.tutorial.message

import org.bukkit.entity.Player

abstract class TutorialMessage(val seconds: Double) {
	/** Show the tutorial message, and give an amount of seconds to wait before displaying the next message */
	abstract fun show(player: Player)
}
