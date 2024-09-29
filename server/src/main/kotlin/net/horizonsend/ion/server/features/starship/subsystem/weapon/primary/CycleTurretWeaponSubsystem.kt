package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.CycleTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import java.util.concurrent.TimeUnit

class CycleTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: CycleTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face) {
    override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.cycleTurret
    override val inaccuracyRadians: Double get() = Math.toRadians(balancing.inaccuracyRadians)
    override val powerUsage: Int get() = balancing.powerUsage
    override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)
    override fun getMaxPerShot(): Int = balancing.maxPerShot
}
