package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.starshipweapon.event.CapitalBeamStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.CapitalBeamCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class CapitalBeamWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
) : WeaponSubsystem(starship, pos),
	ManualWeaponSubsystem,
	HeavyWeaponSubsystem,
	PermissionWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.capitalBeam
	override val permission: String = "ioncore.eventweapon"
	override val powerUsage: Int = balancing.powerUsage
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return true
	}

	override fun getMaxPerShot(): Int = 1

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !starship.isInternallyObstructed(getFirePos(), dir)
	}

	fun getFirePos(): Vec3i {
		return this.pos.plus(Vec3i(0, 10, 0))
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return dir
	}
	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		CapitalBeamCannonProjectile(starship, getFirePos().toLocation(starship.world), dir, shooter).fire()
	}

	override fun isIntact(): Boolean {
		val (x, y, z) = pos
		return CapitalBeamStarshipWeaponMultiblock.blockMatchesStructure(
			starship.world.getBlockAt(x, y, z),
			inward = starship.forward,
			loadChunks = true,
			particles = false
		)
	}
}
