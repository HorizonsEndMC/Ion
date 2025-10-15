package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.PointDefenseBalancing.PointDefenseProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.misc.PointDefenseStarshipWeaponMultiblockSide
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class PointDefenseLaserProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile<PointDefenseProjectileBalancing>(source, name, loc, dir, shooter, PointDefenseStarshipWeaponMultiblockSide.damageType) {
	override val color: Color = Color.BLUE
}
