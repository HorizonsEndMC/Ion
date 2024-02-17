package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar
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

class StarshipsSidebarComponent3(starship: ActiveControlledStarship, player: Player) : SidebarComponent {
    private val pmShield = starship.reactor.powerDistributor.shieldPortion.times(100).toInt()
    private val pmWeapon = starship.reactor.powerDistributor.weaponPortion.times(100).toInt()
    private val pmThruster = starship.reactor.powerDistributor.thrusterPortion.times(100).toInt()
    private val compassComponent = StarshipsSidebar.compassComponent(starship, player)
    private val advancedStarshipInfo = PlayerCache[player.uniqueId].advancedStarshipInfo

    private fun displayPowerMode() : TextComponent {
        return if (advancedStarshipInfo) {
            ofChildren(
                text("PM: ", GRAY),
                StarshipsSidebar.powerModeComponent(pmShield, pmWeapon, pmThruster)
            )
        } else Component.empty()
    }

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            compassComponent[2][0],
            space(),
            compassComponent[2][1],
            space(),
            compassComponent[2][2],
            text(" | ", DARK_GRAY),

            // Power modes
            displayPowerMode()
        )
        drawable.drawLine(line)
    }
}