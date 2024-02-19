package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class PlasmaCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) : CannonWeaponSubsystem(starship, pos, face) {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.plasmaCannon

	override val powerUsage: Int = balancing.powerUsage
	override val length: Int = balancing.length
	override val angleRadiansHorizontal: Double = Math.toRadians(balancing.angleRadiansHorizontal)
	override val angleRadiansVertical: Double = Math.toRadians(balancing.angleRadiansVertical)
	override val convergeDist: Double = balancing.convergeDistance
	override val extraDistance: Int = balancing.extraDistance

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return this.face == starship.forward
	}

	override fun isForwardOnly(): Boolean = balancing.forwardOnly

	override fun getMaxPerShot(): Int {
		return 1
	}

	override fun fire(
        loc: Location,
        dir: Vector,
        shooter: Damager,
        target: Vector?
	) {
		PlasmaLaserProjectile(starship, loc, dir, shooter).fire()
	}
}
