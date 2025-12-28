package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.starship.ArtilleryBalancing
import net.horizonsend.ion.server.configuration.starship.PlasmaCannonBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArtilleryProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector


class ArtilleryWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
) : CannonWeaponSubsystem<ArtilleryBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(ArtilleryWeaponSubsystem::class)) {
	override val length: Int = 3
	override val extraDistance: Int = 3

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		starship.debug("face: $face weapon facing: ${this.face}")
		return super.isAcceptableDirection(face)
	}

	override fun fire(
        loc: Location,
        dir: Vector,
        shooter: Damager,
        target: Vector
	) {
		ArtilleryProjectile(
			StarshipProjectileSource(starship),
			getName(),
			loc,
			dir,
			shooter
		).fire()
	}

	override fun getName(): Component {
		return Component.text("Artillery")
	}
}
