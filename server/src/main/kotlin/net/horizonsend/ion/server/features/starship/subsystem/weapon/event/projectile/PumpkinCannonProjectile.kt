package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.BlockProjectile
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

class PumpkinCannonProjectile(
	starship: ActiveStarship?,
	loc: Location,
	direction: Vector,
	shooter: Damager
) : BlockProjectile(starship, loc, direction, shooter) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.pumpkinCannon ?: IonServer.starshipBalancing.nonStarshipFired.pumpkinCannon
	override val blockMap: Map<Vec3i, BlockData> = faces

	companion object {
		private val faces = CARDINAL_BLOCK_FACES.associate {
			val mod = Vec3i(it.modX, it.modY, it.modZ)
			val jackOLantern = Material.CARVED_PUMPKIN.createBlockData() as Directional

			jackOLantern.facing = it

			mod to jackOLantern
		}
	}

	override val range: Double = balancing.range
	override var speed: Double = balancing.speed
	override val shieldDamageMultiplier: Int = balancing.shieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val soundName: String = balancing.soundName

	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		super.impact(newLoc, block, entity)
		playCustomSound(newLoc, "starship.weapon.rocket.impact", 30)
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		super.moveVisually(oldLocation, newLocation, travel)
		speed += 5.0 * delta
	}
}
