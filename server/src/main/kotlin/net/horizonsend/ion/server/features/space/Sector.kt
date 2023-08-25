package net.horizonsend.ion.server.features.space

import net.horizonsend.ion.server.IonServer
import org.bukkit.World
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

data class Sector(val angle: Int, val distance: Int) {
	companion object {
		val letters = "abcdefghijklmnopqrstuvwxyz".toCharArray()
		val letterIndices: Map<Char, Int> = letters.withIndex().associate { it.value to it.index }

		private fun getDistance(char: Char) = 26 - letterIndices.getOrDefault(char, 2)

		fun getSector(world: World): Sector {
			return getSector(world.name)
		}

		fun getSector(worldName: String): Sector {
			val planet = Space.getPlanet(worldName)
			if (planet != null) {
				return getSector(planet.spaceWorldName)
			}

			var string = worldName

			if (string.length > 9) {
				string = string.substring(9) // remove Andromeda prefix
				val letter = string[string.lastIndex] // get the letter at the end
				val distance = getDistance(letter) // letter represents distance from core
				string = string.substring(0, string.lastIndex) // remove the letter
				val angle = string.toIntOrNull() // get the angle as an integer
				if (angle != null) {
					return Sector(angle, distance)
				}
			}

			if (string != "Andromeda") {
				IonServer.slF4JLogger.warn("Invalid sector $worldName, defaulting to 7b")
			}

			return Sector(7, getDistance('b'))
		}
	}

	fun distance(other: Sector): Double {
		val d1 = distance.toDouble() + 0.5
		val d2 = other.distance.toDouble() + 0.5
		val angle = Math.toRadians((angle - other.angle).toDouble() * 10.0)
		return sqrt(d1.pow(2) + d2.pow(2) - 2 * d1 * d2 * cos(angle))
	}

	override fun toString(): String {
		return "Andromeda Sector $angle${letters[distance]}"
	}
}
