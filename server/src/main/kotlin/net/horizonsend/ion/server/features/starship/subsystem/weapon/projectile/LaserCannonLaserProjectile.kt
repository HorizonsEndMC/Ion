package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.LaserCannonBalancing.LaserCannonProjectileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class LaserCannonLaserProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	damageType: DamageType
) : LaserProjectile<LaserCannonProjectileBalancing>(source, name, loc, dir, shooter, damageType) {
	override val color: Color = Color.YELLOW
}
