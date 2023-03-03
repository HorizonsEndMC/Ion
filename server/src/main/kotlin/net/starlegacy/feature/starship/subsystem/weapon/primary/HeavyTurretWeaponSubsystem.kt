package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.multiblock.starshipweapon.turret.HeavyTurretMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

class HeavyTurretWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: HeavyTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face) {
	override val inaccuracyRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.heavyTurret.inaccuracyRadians)
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.heavyTurret.powerUsage
}
