package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player
import kotlin.math.max

class LocationSidebarComponent(player: Player) : SidebarComponent {
    private val playerX = player.location.blockX
    private val playerY = player.location.blockY
    private val playerZ = player.location.blockZ
    private val playerYaw = player.location.yaw.toInt()
    private val playerPitch = player.location.pitch.toInt()

    private val playerPosition = "x:$playerX y:$playerY z:$playerZ "
    private val playerDirection = "$playerYaw/$playerPitch"
    private val padding = repeatString(" ", max(0,
        MainSidebar.MIN_LENGTH - playerPosition.length - playerDirection.length))

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text(playerPosition, AQUA),
            text(playerDirection, WHITE),
            text(padding)
        )
        drawable.drawLine(line)
    }
}
