package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.missiles

import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.client.display.teleportDuration
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class ArsenalMissileProjectile<B : StarshipTrackingProjectileBalancing>(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	initialDir: Vector,
	override val balancing: B,
	shooter: Damager,
	face: BlockFace, //Up = true, down = false
	originalTarget: Vector,
	baseAimDistance: Int
) : PlayerGuidedLaserProjectile<B>(source, name, loc, dir, initialDir, balancing, shooter, face, originalTarget, baseAimDistance) {
	override val item = ItemFactory.unStackableCustomItem("projectile/activated_arsenal_missile").construct()
	override val color: Color = Color.ORANGE
	override val container = ItemDisplayContainer(
		source.getWorld(),
		4.0F,
		loc.toVector(),
		dir.clone().multiply(-1),
		ItemFactory.unStackableCustomItem("projectile/activated_arsenal_missile").construct(),
		interpolation = 2
	).apply {
		getEntity().transformationInterpolationDuration = 2
		getEntity().teleportDuration = 2
	}
}
