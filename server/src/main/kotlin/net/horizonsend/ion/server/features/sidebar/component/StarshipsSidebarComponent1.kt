package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar.hullIntegrityComponent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player

class StarshipsSidebarComponent1(starship: ActiveControlledStarship, player: Player) : SidebarComponent {
    private val hullIntegrity = starship.hullIntegrity.times(100).toInt()
    private val compassComponent = StarshipsSidebar.compassComponent(starship, player)
    private val advancedStarshipInfo = PlayerCache[player.uniqueId].advancedStarshipInfo

    private fun displayHullIntegrity() : TextComponent {
        return if (advancedStarshipInfo) {
            ofChildren(
                text("HULL: ", GRAY),
                hullIntegrityComponent(hullIntegrity),
                text("%", DARK_GRAY)
            )
        } else Component.empty()
    }

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            compassComponent[0][0],
            space(),
            compassComponent[0][1],
            space(),
            compassComponent[0][2],
            text(" | ", DARK_GRAY),

            // Hull integrity
            displayHullIntegrity()
        )
        drawable.drawLine(line)
    }
}