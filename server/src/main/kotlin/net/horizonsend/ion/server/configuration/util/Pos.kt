package net.horizonsend.ion.server.configuration.util

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

@Serializable
data class Pos(
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int
) {
    fun bukkitWorld(): World = Bukkit.getWorld(world) ?: throw NullPointerException("Could not find world $world")

    fun toVector(): Vector = Vector(x, y, z)

    fun toVec3i(): Vec3i = Vec3i(x, y, z)

    fun toLocation(): Location = Location(bukkitWorld(), x.toDouble(), y.toDouble(), z.toDouble())

    companion object {
        fun fromLocation(location: Location): Pos = Pos(
            location.world.name,
            location.blockX,
            location.blockY,
            location.blockZ
        )
    }
}
