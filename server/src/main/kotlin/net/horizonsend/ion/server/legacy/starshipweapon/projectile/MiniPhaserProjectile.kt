package net.horizonsend.ion.server.legacy.starshipweapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.projectile.ParticleProjectile
import net.starlegacy.util.mcName
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class MiniPhaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.Ion.balancing.starshipWeapons.MiniPhaser.range
	override var speed: Double = IonServer.Ion.balancing.starshipWeapons.MiniPhaser.speed
	override val shieldDamageMultiplier: Int = IonServer.Ion.balancing.starshipWeapons.MiniPhaser.shieldDamageMultiplier
	override val thickness: Double  =IonServer.Ion.balancing.starshipWeapons.MiniPhaser.thickness
	override val explosionPower: Float = IonServer.Ion.balancing.starshipWeapons.MiniPhaser.explosionPower
	override val volume: Int = IonServer.Ion.balancing.starshipWeapons.MiniPhaser.volume
	override val pitch: Float = IonServer.Ion.balancing.starshipWeapons.MiniPhaser.pitch
	override val soundName: String = IonServer.Ion.balancing.starshipWeapons.MiniPhaser.soundName

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val offset = 0.0
		val count = 1
		val extra = 0.0
		val data = null
		loc.world.spawnParticle(Particle.VILLAGER_HAPPY, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
