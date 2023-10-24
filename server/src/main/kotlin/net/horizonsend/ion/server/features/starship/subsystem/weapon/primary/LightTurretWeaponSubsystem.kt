package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.LightTurretMultiblock
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting.AutoTurretTarget
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.StarshipCooldownSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector


class LightTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: LightTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face), AutoWeaponSubsystem, StarshipCooldownSubsystem {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.lightTurret.powerUsage
	override val inaccuracyRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.lightTurret.inaccuracyRadians)

	override val range: Double get() = multiblock.range

	override fun autoFire(target: AutoTurretTarget<*>, dir: Vector) {
    	if (starship.initialBlockCount > 12000) {
			starship.userError("You can't fire light turrets on a ship larger than 12000 blocks!")
			return
		}
		multiblock.shoot(starship.world, pos, face, dir, starship, starship.controller.damager)
	}
	override fun manualFire(
			shooter: Damager,
			dir: Vector,
			target: Vector,
	) {
		if (starship.initialBlockCount > 12000) {
			starship.userError("You can't fire light turrets on a ship larger than 12000 blocks!")
			return
		}
		super.manualFire(shooter, dir, target)
	}
}
