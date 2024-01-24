package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class StarshipsSidebarComponent2(starship: ActiveControlledStarship) : SidebarComponent {
    private val icon = starship.type.icon
    private val currentVelocity = starship.cruiseData.velocity.length().roundToHundredth()
    private val maxVelocity = starship.cruiseData.targetSpeed
    private val pmThruster = starship.reactor.powerDistributor.thrusterPortion
    private val acceleration = starship.cruiseData.getRealAccel(pmThruster).roundToHundredth()
    private val isDirectControlEnabled = starship.isDirectControlEnabled
    private val isCruising = StarshipCruising.isCruising(starship)
    private val isStopped = starship.cruiseData.velocity.lengthSquared() == 0.0

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text("W", GRAY),
            space(),
            text(icon, GRAY).font(Sidebar.fontKey),
            space(),
            text("E", GRAY),
            text(" | ", DARK_GRAY),

            // Speed
            text("SPD: ", GRAY),
            StarshipsSidebar.speedComponent(isDirectControlEnabled, isCruising, isStopped),
            space(),
            StarshipsSidebar.maxSpeedComponent(currentVelocity, maxVelocity, acceleration)
        )
        drawable.drawLine(line)
    }
}