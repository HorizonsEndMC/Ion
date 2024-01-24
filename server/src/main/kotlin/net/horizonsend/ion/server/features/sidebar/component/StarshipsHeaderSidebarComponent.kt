package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar.starshipNameComponent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class StarshipsHeaderSidebarComponent(starship: ActiveControlledStarship) : SidebarComponent {
    private val starshipName = starship.data.name ?: starship.type.displayName
    private val starshipIcon = starship.type.icon

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text("Starship").style(style(BOLD).color(YELLOW)),
            text(" | ", DARK_GRAY),

            // Starship Name
            starshipNameComponent(starshipName, starshipIcon)
        )

        drawable.drawLine(line)
    }
}