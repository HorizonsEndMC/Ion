package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.LaserCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.CannonLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class LaserCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.laserCannon
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
		CannonLaserProjectile(starship, getName(), loc, dir, shooter, LaserCannonStarshipWeaponMultiblock.damageType).fire()
	}

	override val extraDistance: Int = balancing.extraDistance

	override fun getName(): Component {
		return Component.text("Laser Cannon")
	}
}
