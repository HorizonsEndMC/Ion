package net.horizonsend.ion.server.features.starship.active

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.ComponentMessageException
import net.horizonsend.ion.server.features.starship.Mass
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.kyori.adventure.audience.Audience
import net.starlegacy.feature.starship.active.ActiveStarshipHitbox
import org.bukkit.Bukkit
import org.bukkit.World
import kotlin.math.min
import kotlin.math.roundToInt

object ActiveStarshipFactory {
	class StarshipActivationException(message: String) : ComponentMessageException(message)

	fun createControlledStarship(
		feedbackDestination: Audience,
		data: StarshipData,
		blockCol: Collection<Long>,
		carriedShips: Map<StarshipData, LongOpenHashSet>
	): ActiveControlledStarship? {
		Tasks.checkMainThread()

		val blocks = LongOpenHashSet(blockCol)
		if (blocks.isEmpty()) return null

		val type = data.starshipType.actualType
		if (blocks.size < type.minSize || blocks.size > type.maxSize) throw StarshipActivationException("Starship size out of bounds, please redetect.")

		val starship = createStarship(data, blocks, carriedShips)

		starship.multiblockManager.processLoad()
		initSubsystems(feedbackDestination, starship)
		starship.transportManager.processLoad()

		return starship
	}

	private fun createStarship(
		data: StarshipData,
		blocks: LongOpenHashSet,
		carriedShips: Map<StarshipData, LongOpenHashSet>
	): ActiveControlledStarship {
		val world = checkNotNull(Bukkit.getWorld(data.levelName))

		val (centerOfMass, mass) = calculateCenterOfMass(world, blocks)

		val hitbox = ActiveStarshipHitbox(blocks)

		return ActiveControlledStarship(data, blocks, mass, centerOfMass, hitbox, carriedShips)
	}

	private fun calculateCenterOfMass(world: World, blocks: LongOpenHashSet): Pair<Vec3i, Double> {
		val first = blocks.first()
		var minX = blockKeyX(first)
		var minY = blockKeyY(first)
		var minZ = blockKeyZ(first)
		var maxX = minX
		var maxY = minY
		var maxZ = minZ

		var weightX = 0.0
		var weightY = 0.0
		var weightZ = 0.0

		var totalMass = 0.0

		for (key in blocks.iterator()) {
			val x = blockKeyX(key)
			val y = blockKeyY(key)
			val z = blockKeyZ(key)

			if (x < minX) minX = x
			if (x > maxX) maxX = x
			if (y < minY) minY = y
			if (y > maxY) maxY = y
			if (z < minZ) minZ = z
			if (z > maxZ) maxZ = z

			val block = world.getBlockAt(x, y, z)
			val type = block.type

			val mass = Mass[type]
			totalMass += mass
			weightX += x * mass
			weightY += y * mass
			weightZ += z * mass
		}

		val mass = totalMass

		val avgX = weightX / mass
		val avgY = weightY / mass
		val avgZ = weightZ / mass

		return Vec3i(avgX.roundToInt(), avgY.roundToInt(), avgZ.roundToInt()) to mass
	}

	private fun initSubsystems(feedbackDestination: Audience, starship: ActiveControlledStarship) {
		SubsystemDetector.detectSubsystems(feedbackDestination, starship)
		prepareShields(starship)
		starship.generateThrusterMap()
		determineForward(starship)
		fixForwardOnlySubsystems(feedbackDestination, starship) // this can't be done till after forward is found
	}

	private fun determineForward(starship: ActiveStarship) {
		starship.forward = starship.thrusterMap.entries
			.maxByOrNull { it.value.maxSpeed }
			?.key
			?: starship.forward

		starship.multiblockManager.referenceForward = starship.forward
	}

	private fun prepareShields(starship: ActiveControlledStarship) {
		limitReinforcedShields(starship)
	}

	private fun limitReinforcedShields(starship: ActiveControlledStarship) {
		val reinforcedCount = starship.shields.count { it.isReinforcementEnabled }
		val maxReinforced = min(3, starship.initialBlockCount / 7500)

		if (reinforcedCount <= maxReinforced) {
			return
		}

		for (shield in starship.shields) {
			shield.isReinforcementEnabled = false
		}

		// do it after passengers are detected
		Tasks.syncDelay(1L) {
			starship.userError("Enhanced shields enhancements deactivated, found $reinforcedCount but ship only sustains $maxReinforced")
		}
	}

	private fun fixForwardOnlySubsystems(feedbackDestination: Audience, starship: ActiveControlledStarship) {
		for (weapon in starship.weapons.reversed()) {
			if (weapon !is DirectionalSubsystem) {
				continue
			}

			if (!weapon.isForwardOnly()) {
				continue
			}

			val face = weapon.face

			if (face == starship.forward) {
				continue
			}

			starship.weapons.remove(weapon)
			starship.subsystems.remove(weapon)
			val pos = weapon.pos

			feedbackDestination.userErrorAction("${weapon.name} at $pos is facing $face, but is forward-only and forward is ${starship.forward}")
		}
	}
}
