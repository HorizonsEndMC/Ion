package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import fr.skytasul.guardianbeam.Laser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.starship.CthulhuBeamBalancing.CthulhuBeamProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.CthulhuBeamStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HitscanProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.util.Vector

class CthulhuBeamProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
) : HitscanProjectile<CthulhuBeamProjectileBalancing>(source, name, loc, dir, shooter, CthulhuBeamStarshipWeaponMultiblockTop.damageType) {

	override fun drawBeam() {
		val laserEnd = location.clone().add(direction.clone().multiply(range))
		Laser.CrystalLaser(location, laserEnd, 5, -1).durationInTicks().apply { start(IonServer) }
	}
}
