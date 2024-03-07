package net.horizonsend.ion.server.features.ai.util

import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.add
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.asKotlinRandom

abstract class AITarget {
	abstract var offset: Vec3i

	abstract fun getLocation(random: Boolean = false): Location
	abstract fun getVec3i(random: Boolean = false): Vec3i

	abstract fun getWorld(): World
	abstract fun getAutoTurretTarget(): AutoTurretTargeting.AutoTurretTarget<*>

	abstract fun isActive(): Boolean
}

class PlayerTarget(val player: Player) : AITarget() {
	override var offset = Vec3i(0, 0, 0)

	override fun getWorld(): World = player.world

	override fun getLocation(random: Boolean): Location {
		return player.location.add(offset)
	}

	override fun getVec3i(random: Boolean): Vec3i {
		return Vec3i(player.location).plus(offset)
	}

	override fun getAutoTurretTarget(): AutoTurretTargeting.AutoTurretTarget<*> {
		return AutoTurretTargeting.target(player)
	}

	override fun isActive(): Boolean {
		return player.isOnline
	}

	override fun toString(): String {
		return player.name
	}
}

class StarshipTarget(val ship: ActiveStarship) : AITarget() {
	override var offset = Vec3i(0, 0, 0)

	override fun getWorld(): World = ship.world

	override fun getLocation(random: Boolean): Location {
		return getVec3i(random).toLocation(getWorld()).add(offset)
	}

	override fun getVec3i(random: Boolean): Vec3i {
		return if (random) {
			val key = ship.blocks.random(ThreadLocalRandom.current().asKotlinRandom())

			Vec3i(key).plus(offset)
		} else ship.centerOfMass.plus(offset)
	}

	override fun getAutoTurretTarget(): AutoTurretTargeting.AutoTurretTarget<*> {
		return AutoTurretTargeting.target(ship)
	}

	override fun isActive(): Boolean {
		return ActiveStarships.isActive(ship)
	}

	override fun toString(): String {
		return ship.identifier
	}
}
