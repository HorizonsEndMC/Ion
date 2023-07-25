package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class PlasmaCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.plasmaCannon.powerUsage
	override val length: Int = IonServer.balancing.starshipWeapons.plasmaCannon.length
	override val angleRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.plasmaCannon.angleRadians)
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.plasmaCannon.convergeDistance
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.plasmaCannon.extraDistance

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return true
	}

	override fun isForwardOnly(): Boolean = IonServer.balancing.starshipWeapons.plasmaCannon.fowardOnly

	override fun getMaxPerShot(): Int {
		return 1
	}

	override fun fire(
        loc: Location,
        dir: Vector,
        shooter: Controller,
        target: Vector?
	) {
		PlasmaLaserProjectile(starship, loc, dir, shooter).fire()
	}
}
