package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player

class StarshipsSidebarComponent4(private val starship: ActiveControlledStarship, player: Player) : SidebarComponent {
    private val capacitor = (starship.reactor.weaponCapacitor.charge /
            starship.reactor.weaponCapacitor.capacity).times(100).toInt()
    private val boostTime = starship.reactor.heavyWeaponBooster.getWarmupTime()
    private val weaponset = starship.weaponSetSelections[(starship.controller as ActivePlayerController).player.uniqueId]
    private val advancedStarshipInfo = PlayerCache[player.uniqueId].advancedStarshipInfo

    private fun displayWeaponInfo() : TextComponent {
        return if (advancedStarshipInfo) {
            ofChildren(
                // Capacitor
                text("CAP: ", WHITE),
                StarshipsSidebar.capacitorComponent(capacitor),
                text("%", GRAY),
                space(),

                // Heavy weapons charge time
                text("HVY: ", WHITE),
                StarshipsSidebar.heavyWeaponChargeComponent(boostTime),
                space(),

                // Active modules
                text("ACTIVE: ", WHITE),
                StarshipsSidebar.activeModulesComponent(
                    StarshipsSidebar.weaponsetActiveComponent(weaponset),
                    StarshipsSidebar.interdictionActiveComponent(starship.isInterdicting)
                )
            )
        } else Component.empty()
    }

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            displayWeaponInfo()
        )
        drawable.drawLine(line)
    }
}