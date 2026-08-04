package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.PulseCannonBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.PulseCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class PulseLaserProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager
) : LaserProjectile<PulseCannonBalancing.PulseCannonProjectileBalancing>(source, name, loc, dir, shooter, PulseCannonStarshipWeaponMultiblock.damageType)
