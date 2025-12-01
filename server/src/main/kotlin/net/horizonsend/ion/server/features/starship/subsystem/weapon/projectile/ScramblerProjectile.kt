package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.ScramblerBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.ScramblerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class ScramblerProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager
) : LaserProjectile<ScramblerBalancing.ScramblerProjectileBalancing>(source, name, loc, dir, shooter,
	ScramblerStarshipWeaponMultiblock.damageType)
