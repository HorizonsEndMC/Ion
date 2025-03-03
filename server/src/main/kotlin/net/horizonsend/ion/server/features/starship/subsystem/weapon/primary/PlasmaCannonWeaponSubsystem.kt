package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit


class PlasmaCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) : CannonWeaponSubsystem(starship, pos, face) {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.plasmaCannon

	override val powerUsage: Int = balancing.powerUsage
	override val length: Int = balancing.length
	override val angleRadiansHorizontal: Double = Math.toRadians(balancing.angleRadiansHorizontal)
	override val angleRadiansVertical: Double = Math.toRadians(balancing.angleRadiansVertical)
	override val convergeDist: Double = balancing.convergeDistance
	override val extraDistance: Int = balancing.extraDistance
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return this.face == starship.forward
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		if (starship.controller is PlayerController) {
			return !starship.isInternallyObstructed(getFirePos(), dir)
		}
		val firePos = getFirePos()
		val targetDir = target.clone().subtract(firePos.toCenterVector())
		val angle = targetDir.angle(dir)
		//since the direction is already coerced into the firing cone we want to check how much the coercion was
		if (angle > maxOf(angleRadiansVertical,angleRadiansHorizontal) * 0.2) return false
		return !starship.isInternallyObstructed(firePos, dir)
	}

	override fun isForwardOnly(): Boolean = balancing.forwardOnly

	override fun getMaxPerShot(): Int {
		return 1
	}

	override fun fire(
        loc: Location,
        dir: Vector,
        shooter: Damager,
        target: Vector
	) {
		PlasmaLaserProjectile(starship, getName(), loc, dir, shooter).fire()
	}

	override fun getName(): Component {
		return Component.text("Plasma Cannon")
	}
}
