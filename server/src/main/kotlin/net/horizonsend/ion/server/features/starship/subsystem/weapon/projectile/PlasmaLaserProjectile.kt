package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.PlasmaCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.gayColors
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

// from left to right red - orange - yellow - green - blue - purple
class PlasmaLaserProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile<StarshipWeapons.PlasmaCannonBalancing.PlasmaCannonProjectileBalancing>(source, name, loc, dir, shooter, PlasmaCannonStarshipWeaponMultiblock.damageType) {
	override val color: Color get() = if ((source as? StarshipProjectileSource)?.starship?.rainbowToggle == true) gayColors.random() else shooter.color
}
