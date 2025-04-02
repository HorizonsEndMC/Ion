package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.starship.AbyssalGazeBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.AbyssalGazeProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class AbyssalGazeSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : CannonWeaponSubsystem<AbyssalGazeBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier()), PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"
	override val length: Int = 4

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return true
	}

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		AbyssalGazeProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter, target, 30).fire()
	}

	override fun getName(): Component {
		return Component.text("Abyssal Gaze")
	}
}
