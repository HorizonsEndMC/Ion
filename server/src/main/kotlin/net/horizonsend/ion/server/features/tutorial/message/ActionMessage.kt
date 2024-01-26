package net.horizonsend.ion.server.features.tutorial.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

class ActionMessage(
	title: Component = text(""),
	subtitle: Component = text(""),
	private val action: (Player) -> Unit
) : PopupMessage(title, subtitle) {
	override fun show(player: Player) {
		super.show(player)
		action(player)
	}
}
