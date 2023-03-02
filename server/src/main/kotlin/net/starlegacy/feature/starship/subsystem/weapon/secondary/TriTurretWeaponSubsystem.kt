package net.starlegacy.feature.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.multiblock.starshipweapon.turret.TriTurretMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace
import java.util.concurrent.TimeUnit

class TriTurretWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: TriTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face),
	HeavyWeaponSubsystem {
	override val inaccuracyRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.TriTurret.inaccuracyRadians)
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.TriTurret.powerusage
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.balancing.starshipWeapons.TriTurret.boostChargeNanos)
}
