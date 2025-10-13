package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.starship.LaserCannonBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.LaserCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LaserCannonLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class LaserCannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
) : CannonWeaponSubsystem<LaserCannonBalancing>(starship, pos, face, starship.balancingManager.getSubsystemSupplier(LaserCannonWeaponSubsystem::class)) {
	override val length: Int = 2

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		starship.debug("face: $face weapon facing: ${this.face}")
		return super.isAcceptableDirection(face)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		LaserCannonLaserProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter, LaserCannonStarshipWeaponMultiblock.damageType).fire()
	}

	override fun getName(): Component {
		return Component.text("Laser Cannon")
	}
}
