package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.COMPASS_NEEDLE_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.LIST_ICON
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar.starshipNameComponent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player

class StarshipsHeaderSidebarComponent(starship: ActiveControlledStarship, player: Player) : SidebarComponent {
    private val starshipName = starship.data.name ?: starship.type.displayName
    private val starshipIcon = starship.type.icon
    private val advancedStarshipInfo = PlayerCache[player.uniqueId].advancedStarshipInfo
    private val rotateCompass = PlayerCache[player.uniqueId].rotateCompass

    private fun getColor(enabled: Boolean) : NamedTextColor {
        return if (enabled) AQUA else GRAY
    }

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text("Starship").style(style(BOLD).color(YELLOW)),
            text(" | ", DARK_GRAY),
            text(LIST_ICON.text, getColor(advancedStarshipInfo)).font(Sidebar.fontKey),
            space(),
            text(COMPASS_NEEDLE_ICON.text, getColor(rotateCompass)).font(Sidebar.fontKey),
            text(" | ", DARK_GRAY),
            // Starship Name
            starshipNameComponent(starshipName, starshipIcon)
        )
        drawable.drawLine(line)
    }
}