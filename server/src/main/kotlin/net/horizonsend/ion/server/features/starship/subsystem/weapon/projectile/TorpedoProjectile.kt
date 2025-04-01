package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons.TorpedoBalancing.TorpedoProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.TorpedoStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class TorpedoProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	originalTarget: Vector,
	baseAimDistance: Int,
) : TrackingLaserProjectile<TorpedoProjectileBalancing>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, TorpedoStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.fromRGB(255, 0, 255)
}
