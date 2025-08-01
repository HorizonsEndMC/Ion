package net.horizonsend.ion.server.features.multiblock.type.defense.active.projectile

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipSounds
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.damager.damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class AntiAirCannonProjectile(
	loc: Location,
	dir: Vector,
	private val playerShooter: Player
): ParticleProjectile(
	null,
	Component.text("Anti Air Cannon"),
	loc,
	dir,
	playerShooter.damager(),
	DamageType.GENERIC
) {
	override val balancing: StarshipWeapons.ProjectileBalancing = ConfigurationFiles.starshipBalancing().antiAirCannon
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val speed = balancing.speed
	override val range = balancing.range
	override val explosionPower = balancing.explosionPower
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val soundName = balancing.soundName
	override val nearSound: StarshipSounds.SoundInfo = balancing.soundFireNear
	override val farSound: StarshipSounds.SoundInfo = balancing.soundFireFar

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
		loc.world.spawnParticle(Particle.SMOKE, x, y, z, 10, 0.0, 0.0, 0.0, 0.05)
		loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.5, dustOptions, force)
	}
}
