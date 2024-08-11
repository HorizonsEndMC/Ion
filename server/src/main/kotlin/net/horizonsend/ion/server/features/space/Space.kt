package net.horizonsend.ion.server.features.space

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.space.Planet
import net.horizonsend.ion.common.database.schema.space.Star
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.isWater
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.optional
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.util.Optional

object Space : IonServerComponent() {
	private val stars = mutableListOf<CachedStar>()
	private val planets = mutableListOf<CachedPlanet>()

	val planetWorldCache: LoadingCache<World, Optional<CachedPlanet>> =
		CacheBuilder.newBuilder().weakKeys().build(
			CacheLoader.from { world ->
				return@from optional(planets.firstOrNull { it.planetWorld == world })
			}
		)

	val starNameCache: LoadingCache<String, Optional<CachedStar>> =
		CacheBuilder.newBuilder().build(
			CacheLoader.from { name ->
				return@from optional(
					stars.firstOrNull {
						it.id.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true)
					}
				)
			}
		)

	val planetNameCache: LoadingCache<String, Optional<CachedPlanet>> =
		CacheBuilder.newBuilder().build(
			CacheLoader.from { name ->
				return@from optional(
					planets.firstOrNull {
						it.id.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true)
					}
				)
			}
		)

	override fun onEnable() {
		reload()

		listen<WorldUnloadEvent> { event -> planetWorldCache.invalidate(event.world) }

		listen<BlockBreakEvent> { event ->
			val world = event.block.world

			if (!SpaceWorlds.contains(world)) return@listen

			val x = event.block.x.toDouble()
			val y = event.block.y.toDouble()
			val z = event.block.z.toDouble()

			fun check(loc: Vec3i, radius: Int): Boolean {
				return distanceSquared(
					x,
					y,
					z,
					loc.x.toDouble(),
					loc.y.toDouble(),
					loc.z.toDouble()
				) <= radius.squared()
			}

			for (star in getStars()) {
				if (check(star.location, star.outerSphereRadius)) {
					event.isCancelled = true
					return@listen
				}
			}

			for (planet in getPlanets()) {
				if (check(planet.location, planet.atmosphereRadius)) {
					event.isCancelled = true
					return@listen
				}
			}
		}

		listen<BlockFadeEvent> { event ->
			if (!SpaceWorlds.contains(event.block.world)) return@listen

			if (event.newState.type.isWater) event.isCancelled = true
		}
	}

	fun reload() {
		stars.clear()
		planets.clear()

		for (mongoStar: Star in Star.all()) {
			val starId: Oid<Star> = mongoStar._id

			val starName: String = mongoStar.name
			val spaceWorldName: String = mongoStar.spaceWorld
			val starX: Int = mongoStar.x
			val starY = 192 // mongoStar.y
			val starZ: Int = mongoStar.z
			val starSize: Double = mongoStar.size
			val starSeed: Long = mongoStar.seed
			val layers = mongoStar.crustLayers.map { layer ->
				CachedStar.StarCrustLayer(
					layer.index,
					layer.crustNoise,
					layer.materials
						.map { Material.getMaterial(it) ?: error("No material $it!") }
						.map(Bukkit::createBlockData)
				)
			}

			val star = CachedStar(
				databaseId = starId,
				name = starName,
				spaceWorldName = spaceWorldName,
				location = Vec3i(starX, starY, starZ),
				size = starSize,
				seed = starSeed,
				crustLayers = layers
			)

			stars += star

			for (mongoPlanet: Planet in Planet.getOrbiting(starId)) {
				val planetId: Oid<Planet> = mongoPlanet._id

				val planetName = mongoPlanet.name
				val planetWorldName = mongoPlanet.planetWorld
				val rogue = mongoPlanet.rogue

				val x = mongoPlanet.x
				val z = mongoPlanet.z
				val planetSize = mongoPlanet.size
				val orbitDistance = mongoPlanet.orbitDistance
				val orbitSpeed = mongoPlanet.orbitSpeed
				val orbitProgress = mongoPlanet.orbitProgress

				val seed = mongoPlanet.seed

				val crustMaterials: List<BlockData> = mongoPlanet.crustMaterials
					.map { Material.getMaterial(it) ?: error("No material $it!") }
					.map(Material::createBlockData)

				val cloudMaterials: List<BlockData> = mongoPlanet.cloudMaterials
					.map { Material.getMaterial(it) ?: error("No material $it!") }
					.map(Bukkit::createBlockData)

				val cloudDensity = mongoPlanet.cloudDensity

				val crustNoise = mongoPlanet.crustNoise
				val cloudDensityNoise = mongoPlanet.cloudDensityNoise

				val cloudThreshold = mongoPlanet.cloudThreshold
				val cloudNoise = mongoPlanet.cloudNoise

				val description = mongoPlanet.description

				planets += CachedPlanet(
					databaseId = planetId,
					name = planetName,
					sun = star,
					planetWorldName = planetWorldName,
					rogue = rogue,
					x = x,
					z = z,
					size = planetSize,
					orbitDistance = orbitDistance,
					orbitSpeed = orbitSpeed,
					orbitProgress = orbitProgress,
					seed = seed,
					crustMaterials = crustMaterials,
					crustNoise = crustNoise,
					cloudDensity = cloudDensity,
					cloudMaterials = cloudMaterials,
					cloudDensityNoise = cloudDensityNoise,
					cloudThreshold = cloudThreshold,
					cloudNoise = cloudNoise,
					description = description
				)
			}
		}

		with(planetWorldCache) { invalidateAll(); cleanUp() }
		with(planetNameCache) { invalidateAll(); cleanUp() }

		with(starNameCache) { invalidateAll(); cleanUp() }
	}

	fun getStars(): List<CachedStar> = stars

	fun getPlanets(): List<CachedPlanet> = planets

	fun getPlanet(planetWorld: World): CachedPlanet? = planetWorldCache[planetWorld].orElse(null)

	fun getPlanet(planetName: String): CachedPlanet? = planetNameCache[planetName].orElse(null)

	fun getStar(starName: String): CachedStar? = starNameCache[starName].orElse(null)
}
