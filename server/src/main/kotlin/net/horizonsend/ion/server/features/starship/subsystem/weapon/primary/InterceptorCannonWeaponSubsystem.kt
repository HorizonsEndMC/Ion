package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.InterceptorCannonProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit


class InterceptorCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.interceptorCannon
	override val powerUsage: Int = balancing.powerUsage
	override val length: Int = balancing.length
	override val angleRadiansHorizontal: Double = Math.toRadians(balancing.angleRadiansHorizontal)
	override val angleRadiansVertical: Double = Math.toRadians(balancing.angleRadiansVertical)
	override val convergeDist: Double = balancing.convergeDistance
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		starship.debug("face: $face weapon facing: ${this.face}")
		return super.isAcceptableDirection(face)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		InterceptorCannonProjectile(starship, getName(), loc, dir, shooter).fire()
	}

	override val extraDistance: Int = balancing.extraDistance

	override fun getName(): Component {
		return Component.text("Interceptor Cannon")
	}

	override fun getMaxPerShot(): Int = 4

	override fun isForwardOnly(): Boolean = balancing.forwardOnly
}
