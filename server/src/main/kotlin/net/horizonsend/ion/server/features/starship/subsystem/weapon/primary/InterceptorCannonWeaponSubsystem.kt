package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.StarshipWeapons.IncterceptorCannonBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.InterceptorCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class InterceptorCannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
) : CannonWeaponSubsystem<IncterceptorCannonBalancing>(starship, pos, face, starship.balancingManager.getSupplier()) {
	override val length: Int = 2

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		starship.debug("face: $face weapon facing: ${this.face}")
		return super.isAcceptableDirection(face)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		InterceptorCannonProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter).fire()
	}

	override fun getName(): Component {
		return Component.text("Maser Cannon")
	}

	override fun getMaxPerShot(): Int = 4
}
