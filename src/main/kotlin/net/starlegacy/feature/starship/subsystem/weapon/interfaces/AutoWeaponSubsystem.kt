package net.starlegacy.feature.starship.subsystem.weapon.interfaces

import org.bukkit.entity.Player
import org.bukkit.util.Vector

interface AutoWeaponSubsystem {
    val range: Double

    fun autoFire(target: Player, dir: Vector)

    fun shouldTargetRandomBlock(target: Player): Boolean = true
}
