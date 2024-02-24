package net.horizonsend.ion.server.features.tutorial.message

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.miscellaneous.utils.ticks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import org.bukkit.entity.Player

open class PopupMessage(
	private val title: Component = empty(),
	private val subtitle: Component = empty()
) : TutorialMessage("$title $subtitle".split(" ").count().toDouble() * 0.25 + 0.25) {
	override fun show(player: Player) {
		player.showTitle(title(
			title,
			subtitle,
			times(
				10L.ticks(),                    // Fade in
				(Int.MAX_VALUE - 20L).ticks(),  // Sustain
				0L.ticks())                     // Fade out
		))

		player.sendMessage(ofChildren(title, text(" » ", HEColorScheme.HE_DARK_GRAY), subtitle))
	}
}
