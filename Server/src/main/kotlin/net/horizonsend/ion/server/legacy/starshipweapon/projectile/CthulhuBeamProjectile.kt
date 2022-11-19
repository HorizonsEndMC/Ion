package net.horizonsend.ion.server.legacy.starshipweapon.projectile

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
	override val range: Double = 64.0
	override var speed: Double = 1.0
	override val shieldDamageMultiplier: Int = 10
	override val thickness: Double = 1.0
	override val explosionPower: Float = 1.0f
	override val volume: Float = 0.25f
	override val pitch: Float = 2.0f
	override val soundName: String = "minecraft:block.beacon.power_select"

	override fun visualize(loc: Location, targetLocation: Location) {
		val crystal: EnderCrystal = (loc.world.spawnEntity(loc, EntityType.ENDER_CRYSTAL) as EnderCrystal)

		crystal.isShowingBottom = false
		crystal.beamTarget = targetLocation

		Tasks.syncDelay(8) { crystal.remove() }
	}
}