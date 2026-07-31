package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.missiles

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.client.display.teleportDuration
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TrackingLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.lerp
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class RapidHeavyMissileProjectile<B : StarshipTrackingProjectileBalancing>(
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

	override val item by lazy { ItemFactory.Preset.unStackableCustomItem("projectile/activated_heavy_missile").construct()}
	override val color: Color = Color.ORANGE

	override val container by lazy {
		ItemDisplayContainer(
			source.getWorld(),
			2.0F,
			loc.toVector(),
			dir,
			item,
			interpolation = 2
		).apply {
			getEntity().transformationInterpolationDuration = 2
			getEntity().teleportDuration = 2
		}
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		container.position = location.toVector()
		container.heading = direction.clone()
		container.update()

		/*for (lineLoc in oldLocation.alongVector(newLocation.toVector().subtract(oldLocation.toVector()), 5)) {
            lineLoc.world.spawnParticle(Particle.DUST, lineLoc.x, lineLoc.y, lineLoc.z, 1, 0.0, 0.0, 0.0, 0.0, Particle.DustOptions(color, 2f), true)
        }*/

		(0 until 2).forEach { _ ->
			val angle = Math.PI / 24
			val opposite = direction.clone().multiply(-1)
				.rotateAroundX(randomDouble(-angle, angle))
				.rotateAroundY(randomDouble(-angle, angle))
				.rotateAroundZ(randomDouble(-angle, angle))
			location.world.spawnParticle(
				Particle.LARGE_SMOKE,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				1.0,
				null,
				true
			)
			location.world.spawnParticle(
				Particle.FLAME,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				0.25,
				null,
				true
			)
		}
	}


	override var speed = balancing.speed

	override fun onImpact() {
		for (loc in location.circlePoints(2.0, 30, direction)) {
			val radialVector = loc.toVector().subtract(location.toVector()).normalize()
			loc.world.spawnParticle(
				Particle.CLOUD,
				location,
				0,
				radialVector.x,
				radialVector.y,
				radialVector.z,
				1.0,
				null,
				true
			)
		}

		(0 until 20).forEach { _ ->
			val angle = Math.PI / 12
			val opposite = direction.clone().multiply(-1)
				.rotateAroundX(randomDouble(-angle, angle))
				.rotateAroundY(randomDouble(-angle, angle))
				.rotateAroundZ(randomDouble(-angle, angle))
			location.world.spawnParticle(
				Particle.FLAME,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				1.0,
				null,
				true
			)
		}

		(0 until 40).forEach { _ ->
			val angle = Math.PI / 6
			val opposite = direction.clone().multiply(-1)
				.rotateAroundX(randomDouble(-angle, angle))
				.rotateAroundY(randomDouble(-angle, angle))
				.rotateAroundZ(randomDouble(-angle, angle))
			location.world.spawnParticle(
				Particle.LARGE_SMOKE,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				2.0,
				null,
				true
			)
		}

	}
}
