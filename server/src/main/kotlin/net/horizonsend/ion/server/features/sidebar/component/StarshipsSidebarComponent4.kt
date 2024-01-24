package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class StarshipsSidebarComponent4(private val starship: ActiveControlledStarship) : SidebarComponent {
    private val capacitor = (starship.reactor.weaponCapacitor.charge /
            starship.reactor.weaponCapacitor.capacity).times(100).toInt()
    private val boostTime = starship.reactor.heavyWeaponBooster.getWarmupTime()
    private val weaponset = starship.weaponSetSelections[(starship.controller as ActivePlayerController).player.uniqueId]


    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            // Capacitor
            text("CAP: ", GRAY),
            StarshipsSidebar.capacitorComponent(capacitor),
            text("%", DARK_GRAY),
            space(),

            // Heavy weapons charge time
            text("HVY: ", GRAY),
            StarshipsSidebar.heavyWeaponChargeComponent(boostTime),
            space(),

            // Active modules
            text("ACTIVE: ", GRAY),
            StarshipsSidebar.activeModulesComponent(
                StarshipsSidebar.weaponsetActiveComponent(weaponset),
                StarshipsSidebar.interdictionActiveComponent(starship.isInterdicting)
            )
        )
        drawable.drawLine(line)
    }
}