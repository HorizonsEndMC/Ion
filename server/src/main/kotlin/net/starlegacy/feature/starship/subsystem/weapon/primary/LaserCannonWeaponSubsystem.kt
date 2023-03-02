package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.CannonLaserProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class LaserCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.LaserCannon.powerusage
	override val length: Int = IonServer.balancing.starshipWeapons.LaserCannon.length
	override val angleRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.LaserCannon.angleRadians)
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.LaserCannon.convergeDistance

	override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
		CannonLaserProjectile(starship, loc, dir, shooter).fire()
	}

	override val extraDistance: Int = IonServer.balancing.starshipWeapons.LaserCannon.extraDistance
}
