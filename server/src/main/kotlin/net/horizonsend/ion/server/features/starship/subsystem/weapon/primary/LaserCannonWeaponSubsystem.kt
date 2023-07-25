package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.CannonLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class LaserCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.laserCannon.powerUsage
	override val length: Int = IonServer.balancing.starshipWeapons.laserCannon.length
	override val angleRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.laserCannon.angleRadians)
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.laserCannon.convergeDistance

	override fun fire(loc: Location, dir: Vector, shooter: Controller, target: Vector?) {
		CannonLaserProjectile(starship, loc, dir, shooter).fire()
	}

	override val extraDistance: Int = IonServer.balancing.starshipWeapons.laserCannon.extraDistance
}
