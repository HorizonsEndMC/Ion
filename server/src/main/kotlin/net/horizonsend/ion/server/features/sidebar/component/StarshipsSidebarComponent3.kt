package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class StarshipsSidebarComponent3(starship: ActiveControlledStarship) : SidebarComponent {
    private val pmShield = starship.reactor.powerDistributor.shieldPortion.times(100).toInt()
    private val pmWeapon = starship.reactor.powerDistributor.weaponPortion.times(100).toInt()
    private val pmThruster = starship.reactor.powerDistributor.thrusterPortion.times(100).toInt()

    override fun draw(drawable: LineDrawable) {
        val line = text()

        line.append(text("/", GRAY))
        line.appendSpace()
        line.append(text("S", GRAY))
        line.appendSpace()
        line.append(text("\\", GRAY))
        line.append(text(" | ", DARK_GRAY))

        // Power modes
        line.append(text("PM: ", GRAY))
        line.append(text(pmShield, AQUA))
        line.append(text("/", GRAY))
        line.append(text(pmWeapon, RED))
        line.append(text("/", GRAY))
        line.append(text(pmThruster, YELLOW))

        drawable.drawLine(line.build())
    }
}