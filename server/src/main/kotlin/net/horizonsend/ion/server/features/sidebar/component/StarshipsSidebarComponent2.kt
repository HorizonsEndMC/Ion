package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
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
    private val currentVelocity = starship.cruiseData.velocity.length().roundToHundredth()
    private val maxVelocity = starship.cruiseData.targetSpeed
    private val pmThruster = starship.reactor.powerDistributor.thrusterPortion
    private val acceleration = starship.cruiseData.getRealAccel(pmThruster).roundToHundredth()
    private val isDirectControlEnabled = starship.isDirectControlEnabled
    private val isCruising = StarshipCruising.isCruising(starship)
    private val isStopped = starship.cruiseData.velocity.lengthSquared() == 0.0
    private val compassComponent = StarshipsSidebar.compassComponent(starship)

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            compassComponent[1][0],
            space(),
            compassComponent[1][1],
            space(),
            compassComponent[1][2],
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