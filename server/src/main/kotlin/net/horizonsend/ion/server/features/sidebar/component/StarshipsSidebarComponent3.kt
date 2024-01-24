package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class StarshipsSidebarComponent3(starship: ActiveControlledStarship) : SidebarComponent {
    private val pmShield = starship.reactor.powerDistributor.shieldPortion.times(100).toInt()
    private val pmWeapon = starship.reactor.powerDistributor.weaponPortion.times(100).toInt()
    private val pmThruster = starship.reactor.powerDistributor.thrusterPortion.times(100).toInt()

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text("/", GRAY),
            space(),
            text("S", GRAY),
            space(),
            text("\\", GRAY),
            text(" | ", DARK_GRAY),

            // Power modes
            text("PM: ", GRAY),
            StarshipsSidebar.powerModeComponent(pmShield, pmWeapon, pmThruster)
        )
        drawable.drawLine(line)
    }
}