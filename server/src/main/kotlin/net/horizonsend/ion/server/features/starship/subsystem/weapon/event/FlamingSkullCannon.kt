package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.FlamingSkullProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class FlamingSkullCannon(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face),
	HeavyWeaponSubsystem,
	PermissionWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.skullThrower
	override val permission: String = "ioncore.eventweapon"
	override val length: Int = balancing.length
	override val convergeDist: Double = balancing.convergeDistance
	override val extraDistance: Int = balancing.extraDistance
	override val angleRadiansHorizontal: Double = Math.toRadians(balancing.angleRadiansHorizontal)
	override val angleRadiansVertical: Double = Math.toRadians(balancing.angleRadiansVertical)
	override val powerUsage: Int = balancing.powerUsage
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
//		FlamingSkullProjectile(starship, getName(), loc, dir, shooter, target, 10).fire()

		for (n in 0..30) {
			val deviation = Vector(
				randomDouble(0.0, 0.2),
				randomDouble(0.0, 0.2),
				randomDouble(0.0, 0.2)
			)

			FlamingSkullProjectile(starship, getName(), loc, dir.clone().add(deviation), shooter, target, 10).fire()
		}
	}

	override fun getName(): Component {
		return Component.text("Screaming Kin")
	}
}
