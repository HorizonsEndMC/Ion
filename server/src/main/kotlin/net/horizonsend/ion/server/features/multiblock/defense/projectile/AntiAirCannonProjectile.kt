package net.horizonsend.ion.server.features.multiblock.defense.projectile

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.damager.damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class AntiAirCannonProjectile(
	loc: Location,
	dir: Vector,
	private val playerShooter: Player
): ParticleProjectile(
	null,
	loc,
	dir,
	playerShooter.damager()
) {
	override val balancing: StarshipWeapons.ProjectileBalancing = IonServer.starshipBalancing.antiAirCannon
	override val volume: Int = IonServer.starshipBalancing.antiAirCannon.volume
	override val pitch: Float = IonServer.starshipBalancing.antiAirCannon.pitch
	override val speed = IonServer.starshipBalancing.antiAirCannon.speed
	override val range = IonServer.starshipBalancing.antiAirCannon.range
	override val explosionPower = IonServer.starshipBalancing.antiAirCannon.explosionPower
	override val shieldDamageMultiplier = IonServer.starshipBalancing.antiAirCannon.shieldDamageMultiplier
	override val soundName = IonServer.starshipBalancing.antiAirCannon.soundName

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
		loc.world.spawnParticle(Particle.SMOKE_NORMAL, x, y, z, 10, 0.0, 0.0, 0.0, 0.05)
		loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.5, dustOptions, force)
	}
}
