package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.AbyssalGazeProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class AbyssalGazeSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face), PermissionWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.abyssalGaze

	override val permission: String = "ioncore.eventweapon"
	override val length: Int = balancing.length
	override val convergeDist: Double = balancing.convergeDistance
	override val extraDistance: Int = balancing.extraDistance
	override val angleRadiansHorizontal: Double = Math.toRadians(balancing.angleRadiansHorizontal)
	override val angleRadiansVertical: Double = Math.toRadians(balancing.angleRadiansVertical)
	override val powerUsage: Int = balancing.powerUsage
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return true
	}

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		AbyssalGazeProjectile(starship, getName(), loc, dir, shooter, target, 30).fire()
	}

	override fun getName(): Component {
		return Component.text("Abyssal Gaze")
	}
}
