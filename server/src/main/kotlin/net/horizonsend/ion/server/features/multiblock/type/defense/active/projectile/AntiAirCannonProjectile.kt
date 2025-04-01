package net.horizonsend.ion.server.features.multiblock.type.defense.active.projectile

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.configuration.StarshipSounds
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.configuration.StarshipWeapons.StarshipParticleProjectileBalancing
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.damager.damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.Projectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.reflect.KClass

class AntiAirCannonProjectile(
	source: ProjectileSource,
	location: Location,
	direction: Vector,
	private val playerShooter: Player
): ParticleProjectile<StarshipParticleProjectileBalancing>(
	source,
	Component.text("Anti Air Cannon"),
	location,
	direction,
	playerShooter.damager(),
	DamageType.GENERIC
) {
	override val balancing = object : StarshipParticleProjectileBalancing {
		override val particleThickness: Double = TODO("Not yet implemented")
		override val clazz: KClass<out Projectile> = TODO("Not yet implemented")
		override val range: Double = TODO("Not yet implemented")
		override val speed: Double = TODO("Not yet implemented")
		override val explosionPower: Float = TODO("Not yet implemented")
		override val starshipShieldDamageMultiplier: Double = TODO("Not yet implemented")
		override val areaShieldDamageMultiplier: Double = TODO("Not yet implemented")
		override val entityDamage: StarshipWeapons.ProjectileBalancing.EntityDamage = TODO("Not yet implemented")
		override val fireSound: StarshipSounds.SoundInfo = TODO("Not yet implemented")
	}

	private fun getColor(shooter: Player): Color {
		val nation: Oid<Nation>? = PlayerCache[shooter].nationOid

		if (nation != null) {
			return Color.fromRGB(NationCache[nation].color)
		}

		return Color.FUCHSIA
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val particle = Particle.DUST_COLOR_TRANSITION
		val color = getColor(playerShooter)

		val dustOptions = Particle.DustTransition(color, Color.BLACK,balancing.particleThickness.toFloat() * 4f)
		location.world.spawnParticle(Particle.SMOKE, x, y, z, 10, 0.0, 0.0, 0.0, 0.05)
		location.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.5, dustOptions, force)
	}
}
