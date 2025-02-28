package net.horizonsend.ion.server.features.ai.util

import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.add
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

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as PlayerTarget

		return player.uniqueId == other.player.uniqueId
	}

	override fun hashCode(): Int {
		return player.uniqueId.hashCode()
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

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as StarshipTarget

		return ship == other.ship
	}

	override fun hashCode(): Int {
		return ship.hashCode()
	}
}

class GoalTarget(val position : Vec3i, private val world: World, var hyperspace : Boolean) : AITarget() {
	override var offset: Vec3i = Vec3i(0,0,0)

	override fun getLocation(random: Boolean): Location {
		return position.toLocation(world)
	}

	override fun getVec3i(random: Boolean): Vec3i {
		return position
	}

	override fun getWorld(): World {
		return world
	}

	override fun getAutoTurretTarget(): AutoTurretTargeting.AutoTurretTarget<*> {
		TODO("Not yet implemented") //need to fix this
	}

	override fun isActive(): Boolean {
		return true
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as GoalTarget

		if (position != other.position) return false
		if (world != other.world) return false

		return true
	}

	override fun hashCode(): Int {
		var result = position.hashCode()
		result = 31 * result + world.hashCode()
		return result
	}
}
