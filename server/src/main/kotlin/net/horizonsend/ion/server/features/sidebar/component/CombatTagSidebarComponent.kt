package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.player.CombatTimer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

class CombatTagSidebarComponent(player: Player) : SidebarComponent {
	private val npcCombatTagTime = CombatTimer.npcTimerRemainingMillis(player)
	private val pvpCombatTagTime = CombatTimer.pvpTimerRemainingMillis(player)

	private fun headerComponent() : Component {
		return ofChildren(
			text("Combat").style(style(BOLD).color(YELLOW)),
			text(" | ", DARK_GRAY)
		)
	}

	private fun timeComponent(time: Long, color: NamedTextColor) : Component {
		val minutes = TimeUnit.MILLISECONDS.toMinutes(time)
		val seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes)
		return text("${minutes}m ${seconds.toString().padStart(2, '0')}s", color)
	}

	private fun displayCombatTimerInfo() : TextComponent {
		return ofChildren(
			headerComponent(),
			if (npcCombatTagTime > 0) {
				ofChildren(
					text("NPC: ", NamedTextColor.GRAY),
					timeComponent(npcCombatTagTime, NamedTextColor.GOLD),
					Component.space()
				)
			} else Component.empty(),

			if (pvpCombatTagTime > 0) {
				ofChildren(
					text("PvP: ", NamedTextColor.GRAY),
					timeComponent(pvpCombatTagTime, NamedTextColor.DARK_RED),
				)
			} else Component.empty()
		)
	}

	override fun draw(drawable: LineDrawable) {
		val line = displayCombatTimerInfo()
		drawable.drawLine(line)
	}
}
