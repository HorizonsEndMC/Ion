package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PlasmaCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val powerUsage: Int = IonServer.Ion.balancing.starshipWeapons.PlasmaCannon.powerusage
	override val length: Int = IonServer.Ion.balancing.starshipWeapons.PlasmaCannon.length
	override val angleRadians: Double = Math.toRadians(IonServer.Ion.balancing.starshipWeapons.PlasmaCannon.angleRadians)
	override val convergeDist: Double = IonServer.Ion.balancing.starshipWeapons.PlasmaCannon.convergeDistance
	override val extraDistance: Int = IonServer.Ion.balancing.starshipWeapons.PlasmaCannon.extraDistance

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return true
	}

	override fun isForwardOnly(): Boolean = true

	override fun getMaxPerShot(): Int {
		return 1
	}

	override fun fire(
		loc: Location,
		dir: Vector,
		shooter: Player,
		target: Vector?
	) {
		PlasmaLaserProjectile(starship, loc, dir, shooter).fire()
	}
}
