package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.TriTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class TriTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: TriTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face),
	HeavyWeaponSubsystem, AutoWeaponSubsystem {
	override val inaccuracyRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.triTurret.inaccuracyRadians)
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.triTurret.powerUsage
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.balancing.starshipWeapons.triTurret.boostChargeNanos)

	override val range: Double get() = multiblock.range

	override fun autoFire(target: Player, dir: Vector) {
		multiblock.shoot(starship.world, pos, face, dir, starship, starship.controller)
	}
}
