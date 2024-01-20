package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class StarshipsSidebarComponent2(private val starship: ActiveControlledStarship) : SidebarComponent {
    private val icon = starship.type.icon
    private val currentVelocity = starship.cruiseData.velocity.length().roundToHundredth()
    private val maxVelocity = starship.cruiseData.targetSpeed
    private val pmThruster = starship.reactor.powerDistributor.thrusterPortion
    private val acceleration = starship.cruiseData.getRealAccel(pmThruster).roundToHundredth()

    override fun draw(drawable: LineDrawable) {
        val line = text()

        line.append(text("W", GRAY))
        line.appendSpace()
        line.append(text(icon, GRAY).font(Sidebar.fontKey))
        line.appendSpace()
        line.append(text("E", GRAY))
        line.append(text(" | ", DARK_GRAY))

        // Speed
        line.append(text("SPD: ", GRAY))
        if (starship.isDirectControlEnabled) {
            line.append(text("DC", GOLD))
            line.appendSpace()
        }
        if (StarshipCruising.isCruising(starship)) {
            line.append(text("»", GREEN))
        } else {
            if (starship.cruiseData.velocity.lengthSquared() != 0.0) {
                line.append(text("«", RED))
            } else {
                line.append(text("□", DARK_GRAY))
            }
        }
        line.appendSpace()
        line.append(text(currentVelocity, GREEN))
        line.append(text("/", DARK_GRAY))
        line.append(text(maxVelocity, DARK_GREEN))
        line.appendSpace()
        line.append(text(acceleration, YELLOW))

        drawable.drawLine(line.build())
    }
}