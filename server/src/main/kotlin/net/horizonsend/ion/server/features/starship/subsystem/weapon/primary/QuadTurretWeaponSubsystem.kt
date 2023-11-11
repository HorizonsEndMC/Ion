package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary


import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.QuadTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.StarshipCooldownSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class QuadTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: QuadTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face), StarshipCooldownSubsystem {
	override val inaccuracyRadians: Double get() = Math.toRadians(IonServer.balancing.starshipWeapons.quadTurret.inaccuracyRadians)
	override val powerUsage: Int get() = IonServer.balancing.starshipWeapons.quadTurret.powerUsage
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(IonServer.balancing.starshipWeapons.quadTurret.fireCooldownNanos)
	override val maxPerShot = when (starship.type) {
		StarshipType.BATTLECRUISER -> 3,
		StarshipType.BATTLESHIP -> 5,
		StarshipType.DREADNOUGHT -> 8
	}

	override fun manualFire(
		shooter: Damager,
		dir: Vector,
		target: Vector,
	) {
		if (starship.initialBlockCount < 16000) {
			starship.userError("You can't fire quad turrets on a ship smaller than 16000 blocks!")
			return
		}
		super.manualFire(shooter, dir, target)
	}
}
