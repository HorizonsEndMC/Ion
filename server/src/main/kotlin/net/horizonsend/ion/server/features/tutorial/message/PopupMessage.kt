package net.horizonsend.ion.server.features.tutorial.message

import net.horizonsend.ion.server.miscellaneous.utils.colorize
import net.horizonsend.ion.server.miscellaneous.utils.msg
import org.bukkit.entity.Player

open class PopupMessage(private val title: String = "", private val subtitle: String = "") :
	TutorialMessage("$title $subtitle".split(" ").count().toDouble() * 0.25 + 0.25) {
	override fun show(player: Player) {
		player.sendTitle(title.colorize(), subtitle.colorize(), 10, Int.MAX_VALUE - 20, 0)
		player msg "&r$title &8>> &r$subtitle"
	}
}
