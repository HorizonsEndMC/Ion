package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.CannonLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class LaserCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.laserCannon
	override val powerUsage: Int = balancing.powerUsage
	override val length: Int = balancing.length
	override val angleRadians: Double = Math.toRadians(balancing.angleRadians)
	override val convergeDist: Double = balancing.convergeDistance

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector?) {
		CannonLaserProjectile(starship, loc, dir, shooter).fire()
	}

	override val extraDistance: Int = balancing.extraDistance
}
