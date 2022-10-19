package net.starlegacy.feature.starship.active

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import kotlin.math.min
import kotlin.math.roundToInt
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.feature.starship.Mass
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import org.bukkit.Bukkit

object ActiveStarshipFactory {
	fun createPlayerStarship(
		data: PlayerStarshipData,
		blockCol: Collection<Long>,
		carriedShips: Map<PlayerStarshipData, LongOpenHashSet>
	): ActivePlayerStarship? {
		Tasks.checkMainThread()

		val blocks = LongOpenHashSet(blockCol)
		if (blocks.isEmpty()) return null

		val starship = createStarship(data, blocks, carriedShips)

		initSubsystems(starship)

		return starship
	}

	private fun createStarship(
		data: PlayerStarshipData,
		blocks: LongOpenHashSet,
		carriedShips: Map<PlayerStarshipData, LongOpenHashSet>
	): ActivePlayerStarship {
		val world = checkNotNull(Bukkit.getWorld(data.world))

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

		val centerOfMass = Vec3i(avgX.roundToInt(), avgY.roundToInt(), avgZ.roundToInt())

		val hitbox = ActiveStarshipHitbox(blocks)

		return ActivePlayerStarship(data, blocks, mass, centerOfMass, hitbox, carriedShips)
	}

	private fun initSubsystems(starship: ActivePlayerStarship) {
		SubsystemDetector.detectSubsystems(starship)
		prepareShields(starship)
		starship.generateThrusterMap()
		determineForward(starship)
		fixForwardOnlySubsystems(starship) // this can't be done till after forward is found
	}

	private fun determineForward(starship: ActiveStarship) {
		starship.forward = starship.thrusterMap.entries
			.maxByOrNull { it.value.maxSpeed }
			?.key
			?: starship.forward
	}

	private fun prepareShields(starship: ActivePlayerStarship) {
		limitReinforcedShields(starship)
	}

	private fun limitReinforcedShields(starship: ActivePlayerStarship) {
		val reinforcedCount = starship.shields.count { it.isReinforcementEnabled }
		val maxReinforced = min(3, starship.blockCount / 7500)

		if (reinforcedCount <= maxReinforced) {
			return
		}

		for (shield in starship.shields) {
			shield.isReinforcementEnabled = false
		}

		// do it after passengers are detected
		Tasks.syncDelay(1L) {
			starship.sendMessage("&cEnhanced shields enhancements deactivated, found $reinforcedCount but ship only sustains $maxReinforced")
		}
	}

	private fun fixForwardOnlySubsystems(starship: ActivePlayerStarship) {
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
			starship.sendMessage("&c${weapon.name} at $pos is facing $face, but is forward-only and forward is ${starship.forward}")
		}
	}
}
