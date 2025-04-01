package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.FlamethrowerProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class FlamethrowerWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
) : CannonWeaponSubsystem<StarshipWeapons.FlamethrowerCannonBalancing>(starship, pos, face, starship.balancingManager.getSupplier()), PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"
	override val length: Int = 8

	override fun canFire(dir: Vector, target: Vector): Boolean {
		if (starship.playerPilot?.hasPermission("ioncore.eventweapon") == false) return false

		return super.canFire(dir, target)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		FlamethrowerProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter).fire()
	}

	override val extraDistance: Int = 3

	override fun getName(): Component {
		return Component.text("Flaming Breath")
	}
}
