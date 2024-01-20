package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class StarshipsSidebarComponent4(private val starship: ActiveControlledStarship) : SidebarComponent {
    private val capacitor = (starship.reactor.weaponCapacitor.charge /
            starship.reactor.weaponCapacitor.capacity).times(100).toInt()
    private val boostTime = starship.reactor.heavyWeaponBooster.getWarmupTime()

    private fun percentColor(percent: Int): NamedTextColor = when {
        percent <= 5 -> RED
        percent <= 10 -> GOLD
        percent <= 25 -> YELLOW
        percent <= 40 -> GREEN
        percent <= 55 -> DARK_GREEN
        percent <= 70 -> AQUA
        percent <= 85 -> DARK_AQUA
        else -> BLUE
    }

    override fun draw(drawable: LineDrawable) {
        val line = text()

        // Capacitor
        line.append(text("CAP: ", GRAY))
        line.append(text(capacitor).color(percentColor(capacitor)))
        line.append(text("%", DARK_GRAY))
        line.appendSpace()

        // Heavy weapons charge time
        line.append(text("HVY: ", GRAY))
        if (boostTime.toInt() == -1) {
            line.append(text("N/A", RED))
        } else if (boostTime > 0) {
            line.append(text(boostTime.nanoseconds.toString(DurationUnit.SECONDS, 1), GOLD))
        } else {
            line.append(text("RDY", GREEN))
        }
        line.appendSpace()

        // Active modules
        line.append(text("ACTIVE: ", GRAY))
        if (starship.isInterdicting) {
            line.append(text("\uE033", AQUA).font(Sidebar.fontKey))
            line.appendSpace()
        }
        val weaponset = starship.weaponSetSelections[starship.playerPilot?.uniqueId]
        if (weaponset != null) {
            line.append(text("\uE026", AQUA).font(Sidebar.fontKey))
            line.append(text(weaponset, AQUA))
            line.appendSpace()
        }

        drawable.drawLine(line.build())
    }
}