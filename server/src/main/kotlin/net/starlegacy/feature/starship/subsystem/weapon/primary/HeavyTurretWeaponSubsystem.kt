package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.starlegacy.feature.multiblock.starshipweapon.turret.HeavyTurretMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class HeavyTurretWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: HeavyTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face) {
	override val inaccuracyRadians: Double get() = Math.toRadians(IonServer.balancing.starshipWeapons.heavyTurret.inaccuracyRadians)
	override val powerUsage: Int get() = IonServer.balancing.starshipWeapons.heavyTurret.powerUsage / 3

	override fun manualFire(
		shooter: Controller,
		dir: Vector,
		target: Vector
	) {
		if (starship.blocks.size < 6500) {
			shooter.userError("You can't fire HTs on a ship smaller than 6500 blocks!")
			return
		}

		super.manualFire(shooter, dir, target)
	}
}
