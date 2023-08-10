package net.horizonsend.ion.server.features.sidebar.tasks

import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.abs

object PlayerLocation {
    fun getPlayerDirection(player: Player): String {
        val playerDirection: Vector = player.location.direction
        playerDirection.setY(0).normalize()

        val directionString = StringBuilder(" ")

        if (playerDirection.z != 0.0 && abs(playerDirection.z) > 0.4) {
            directionString.append(if (playerDirection.z > 0) "south" else "north")
        }

        if (playerDirection.x != 0.0 && abs(playerDirection.x) > 0.4) {
            directionString.append(if (playerDirection.x > 0) "east" else "west")
        }

        return directionString.toString()
    }
}