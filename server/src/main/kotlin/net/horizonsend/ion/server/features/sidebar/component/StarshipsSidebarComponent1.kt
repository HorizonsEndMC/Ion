package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class StarshipsSidebarComponent1(starship: ActiveControlledStarship) : SidebarComponent {
    private val hullIntegrity = starship.hullIntegrity.times(100).toInt()

    override fun draw(drawable: LineDrawable) {
        val line = text()

        line.append(text("\\", GRAY))
        line.appendSpace()
        line.append(text("N", GRAY))
        line.appendSpace()
        line.append(text("/", GRAY))
        line.append(text(" | ", DARK_GRAY))

        // Hull integrity
        line.append(text("HULL: ", GRAY))
        line.append(text(hullIntegrity).run { when {
            hullIntegrity == 100 -> return@run this.color(GREEN)
            hullIntegrity > 90 -> return@run this.color(GOLD)
            hullIntegrity > 85 -> return@run this.color(RED)
            else -> return@run this.color(DARK_RED)
        }})
        line.append(text("%", DARK_GRAY))
        line.appendSpace()

        drawable.drawLine(line.build())
    }
}