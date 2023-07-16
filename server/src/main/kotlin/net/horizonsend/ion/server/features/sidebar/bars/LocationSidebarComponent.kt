package net.horizonsend.ion.server.features.sidebar.bars

import net.horizonsend.ion.server.miscellaneous.repeatString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player
import kotlin.math.max

class LocationSidebarComponent(private val player: Player) : SidebarComponent {
    override fun draw(drawable: LineDrawable) {
        val playerX = player.location.blockX
        val playerY = player.location.blockY
        val playerZ = player.location.blockZ
        val playerYaw = player.location.yaw.toInt()
        val playerPitch = player.location.pitch.toInt()

        val playerPosition = "x:$playerX y:$playerY z:$playerZ "
        val playerDirection = "$playerYaw/$playerPitch"
        val padding = repeatString(" ", max(0, MainSidebar.MIN_LENGTH - playerPosition.length - playerDirection.length))

        val line = Component.text()
            .append(Component.text(playerPosition).color(NamedTextColor.AQUA))
            .append(Component.text(playerDirection).color(NamedTextColor.GRAY))
            .append(Component.text(padding))
            .build()

        drawable.drawLine(line)
    }
}
