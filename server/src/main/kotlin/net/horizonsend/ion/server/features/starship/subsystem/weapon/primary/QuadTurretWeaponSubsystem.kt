package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary


import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.QuadTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class QuadTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: QuadTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face) {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.quadTurret
	override val inaccuracyRadians: Double get() = Math.toRadians(balancing.inaccuracyRadians)
	override val powerUsage: Int get() = balancing.powerUsage
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)
	override fun getMaxPerShot(): Int = balancing.maxPerShot

	override fun manualFire(
		shooter: Damager,
		dir: Vector,
		target: Vector,
	) {
		if (starship.initialBlockCount < 18500) {
			starship.userError("You can't fire quad turrets on a ship smaller than 18500 blocks!")
			return
		}
		super.manualFire(shooter, dir, target)
	}
}
