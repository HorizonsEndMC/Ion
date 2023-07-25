package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.multiblock.starshipweapon.turret.LightTurretMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class LightTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: LightTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face), AutoWeaponSubsystem {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.lightTurret.powerUsage
	override val inaccuracyRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.lightTurret.inaccuracyRadians)

	override val range: Double get() = multiblock.range

	override fun autoFire(target: Player, dir: Vector) {
		multiblock.shoot(starship.serverLevel.world, pos, face, dir, starship, starship.controller)
	}
}
