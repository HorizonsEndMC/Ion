package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.QuadTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class QuadTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: QuadTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face) {
	override val inaccuracyRadians: Double get() = Math.toRadians(IonServer.balancing.starshipWeapons.quadTurret.inaccuracyRadians)
	override val powerUsage: Int get() = IonServer.balancing.starshipWeapons.quadTurret.powerUsage

	override fun manualFire(
		shooter: Controller,
		dir: Vector,
		target: Vector
	) {
		if (starship.initialBlockCount < 16000) {
			shooter.userError("You can't fire quad turrets on a ship smaller than 16000 blocks!")
			return
		}

		super.manualFire(shooter, dir, target)
	}
}
