package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.COMPASS_NEEDLE_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.LIST_ICON
import net.horizonsend.ion.server.features.sidebar.tasks.StarshipsSidebar.starshipNameComponent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player

class StarshipsHeaderSidebarComponent(starship: ActiveControlledStarship, player: Player) : SidebarComponent {
    private val starshipName = starship.getDisplayNamePlain()
    private val starshipIcon = starship.type.icon
    private val advancedStarshipInfo = player.getSettingOrThrow(PlayerSettings::advancedStarshipInfo)
    private val rotateCompass = player.getSettingOrThrow(PlayerSettings::rotateCompass)

    private fun getColor(enabled: Boolean) : NamedTextColor {
        return if (enabled) AQUA else GRAY
    }

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text("Starship").style(style(BOLD).color(YELLOW)),
            text(" | ", DARK_GRAY),
            text(LIST_ICON.text, getColor(advancedStarshipInfo)).font(Sidebar.fontKey),
            space(),
            text(COMPASS_NEEDLE_ICON.text, getColor(rotateCompass)).font(Sidebar.fontKey),
            text(" | ", DARK_GRAY),
            // Starship Name
            starshipNameComponent(starshipName, starshipIcon)
        )
        drawable.drawLine(line)
    }
}
