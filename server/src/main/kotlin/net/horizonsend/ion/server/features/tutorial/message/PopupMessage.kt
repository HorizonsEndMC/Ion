package net.horizonsend.ion.server.features.tutorial.message

import io.papermc.paper.util.Tick
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

open class PopupMessage(
	private val title: Component = empty(),
	private val subtitle: Component = empty()
) : TutorialMessage("$title $subtitle".split(" ").count().toDouble() * 0.25 + 0.25) {
	override fun show(player: Player) {
		player.showTitle(
			Title.title(
				title,
				subtitle,
				Title.Times.times(
					Duration.of(10, Tick.tick()),
					Duration.of(Int.MAX_VALUE - 20L, Tick.tick()),
					Duration.of(0,  Tick.tick()))
			)
		)

		player.sendMessage(ofChildren(title, text(" » ", HEColorScheme.HE_DARK_GRAY), subtitle))
	}
}
