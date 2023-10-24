package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.HeavyTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.StarshipCooldownSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class HeavyTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: HeavyTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face), StarshipCooldownSubsystem {
	override val inaccuracyRadians: Double get() = Math.toRadians(IonServer.balancing.starshipWeapons.heavyTurret.inaccuracyRadians)
	override val powerUsage: Int get() = IonServer.balancing.starshipWeapons.heavyTurret.powerUsage / 3

	override fun manualFire(
        shooter: Damager,
        dir: Vector,
        target: Vector
	) {
		if (starship.initialBlockCount < 6500 || starship.initialBlockCount > 12000) {
			starship.userError("You can't fire heavy turrets on a ship smaller than 6500 blocks or larger than 12000 blocks!")
			return
		}

		super.manualFire(shooter, dir, target)
	}
}
