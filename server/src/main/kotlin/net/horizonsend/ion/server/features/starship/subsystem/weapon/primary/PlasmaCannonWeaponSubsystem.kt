package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.PlasmaCannonBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector


class PlasmaCannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
) : CannonWeaponSubsystem<PlasmaCannonBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(PlasmaCannonWeaponSubsystem::class)) {
	override val length: Int = 3

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return this.face == starship.forward
	}

	override fun getMaxPerShot(): Int {
		return 1
	}

	override fun fire(
        loc: Location,
        dir: Vector,
        shooter: Damager,
        target: Vector
	) {
		PlasmaLaserProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter).fire()
	}

	override fun getName(): Component {
		return Component.text("Plasma Cannon")
	}
}
