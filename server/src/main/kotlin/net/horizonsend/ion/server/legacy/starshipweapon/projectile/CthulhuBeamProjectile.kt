package net.horizonsend.ion.server.legacy.starshipweapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Tasks
import org.bukkit.Location
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class CthulhuBeamProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : RayTracedProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.CthulhuBeam.range
	override var speed: Double = IonServer.balancing.starshipWeapons.CthulhuBeam.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.CthulhuBeam.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.CthulhuBeam.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.CthulhuBeam.explosionPower
	override val volume: Float = IonServer.balancing.starshipWeapons.CthulhuBeam.volume.toFloat()
	override val pitch: Float = IonServer.balancing.starshipWeapons.CthulhuBeam.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.CthulhuBeam.soundName

	override fun visualize(loc: Location, targetLocation: Location) {
		val crystal: EnderCrystal = (loc.world.spawnEntity(loc, EntityType.ENDER_CRYSTAL) as EnderCrystal)

		crystal.isShowingBottom = false
		crystal.beamTarget = targetLocation

		Tasks.syncDelay(8) { crystal.remove() }
	}
}
