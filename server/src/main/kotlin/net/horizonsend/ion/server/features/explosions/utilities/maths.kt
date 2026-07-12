package net.horizonsend.ion.server.features.explosions.utilities

import org.bukkit.util.Vector

fun Double.lerp(target: Double, factor: Double): Double {
    return this + (target - this) * factor
}

fun randomDirection(): Vector {
    return Vector(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).normalize()
}
