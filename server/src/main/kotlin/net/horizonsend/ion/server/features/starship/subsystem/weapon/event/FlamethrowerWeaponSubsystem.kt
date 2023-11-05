package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.FlamethrowerProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class FlamethrowerWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face),
	PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"

	override val powerUsage: Int = IonServer.balancing.starshipWeapons.laserCannon.powerUsage
	override val length: Int = 8
	override val angleRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.laserCannon.angleRadians)
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.laserCannon.convergeDistance

	override fun canFire(dir: Vector, target: Vector): Boolean {
		if (starship.playerPilot?.hasPermission("ioncore.eventweapon") == false) return false

		return super.canFire(dir, target)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector?) {
		FlamethrowerProjectile(starship, loc, dir, shooter).fire()
	}

	override val extraDistance: Int = 3
}
