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
	override val range: Double = IonServer.balancing.starshipWeapons.cthulhuBeam.range
	override var speed: Double = IonServer.balancing.starshipWeapons.cthulhuBeam.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.cthulhuBeam.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.cthulhuBeam.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.cthulhuBeam.explosionPower
	override val volume: Float = IonServer.balancing.starshipWeapons.cthulhuBeam.volume.toFloat()
	override val pitch: Float = IonServer.balancing.starshipWeapons.cthulhuBeam.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.cthulhuBeam.soundName

	override fun visualize(loc: Location, targetLocation: Location) {
		val crystal: EnderCrystal = (loc.world.spawnEntity(loc, EntityType.ENDER_CRYSTAL) as EnderCrystal)

		crystal.isShowingBottom = false
		crystal.beamTarget = targetLocation

		Tasks.syncDelay(8) { crystal.remove() }
	}
}
