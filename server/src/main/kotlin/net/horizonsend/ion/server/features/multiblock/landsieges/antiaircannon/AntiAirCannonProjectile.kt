package net.horizonsend.ion.server.features.multiblock.landsieges.antiaircannon

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.feature.starship.subsystem.weapon.projectile.ParticleProjectile
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
	null
) {
	override val volume: Int = IonServer.balancing.starshipWeapons.aaGun.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.aaGun.pitch
	override val speed = IonServer.balancing.starshipWeapons.aaGun.speed
	override val range = IonServer.balancing.starshipWeapons.aaGun.range
	override val explosionPower = IonServer.balancing.starshipWeapons.aaGun.explosionPower
	override val shieldDamageMultiplier = IonServer.balancing.starshipWeapons.aaGun.shieldDamageMultiplier
	override val thickness = IonServer.balancing.starshipWeapons.aaGun.particleThickness
	override val soundName = IonServer.balancing.starshipWeapons.aaGun.soundName

	override fun shouldPlayImpactSound() = true

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

		val dustOptions = Particle.DustTransition(color, Color.BLACK,thickness.toFloat() * 4f)
		loc.world.spawnParticle(Particle.SMOKE_NORMAL, x, y, z, 10, 0.0, 0.0, 0.0, 0.05)
		loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.5, dustOptions, force)
	}
}
