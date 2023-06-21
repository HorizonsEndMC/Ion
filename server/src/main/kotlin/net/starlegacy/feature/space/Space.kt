package net.starlegacy.feature.space

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.starlegacy.SLComponent
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.space.Moon
import net.horizonsend.ion.server.database.schema.space.Planet
import net.horizonsend.ion.server.database.schema.space.Star
import net.starlegacy.listen
import net.starlegacy.util.Vec3i
import net.starlegacy.util.optional
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.event.world.WorldUnloadEvent
import java.util.Optional

object Space : SLComponent() {
	private val stars = mutableListOf<CachedStar>()
	private val planets = mutableListOf<CachedPlanet>()
	private val moons = mutableListOf<CachedMoon>()

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

	val moonNameCache: LoadingCache<String, Optional<CachedMoon>> =
		CacheBuilder.newBuilder().build(
			CacheLoader.from { name ->
				return@from optional(
					moons.firstOrNull {
						it.id.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true)
					}
				)
			}
		)

	val moonWorldCache: LoadingCache<World, Optional<CachedMoon>> =
		CacheBuilder.newBuilder().weakKeys().build(
			CacheLoader.from { world ->
				return@from optional(moons.firstOrNull { it.planetWorld == world })
			}
		)

	override fun onEnable() {
		reload()

		listen<WorldUnloadEvent> { event -> planetWorldCache.invalidate(event.world) }
	}

	fun reload() {
		stars.clear()
		planets.clear()
		moons.clear()

		for (mongoStar: Star in Star.all()) {
			val starId: Oid<Star> = mongoStar._id

			val starName: String = mongoStar.name
			val spaceWorldName: String = mongoStar.spaceWorld
			val starX: Int = mongoStar.x
			val starY: Int = mongoStar.y
			val starZ: Int = mongoStar.z
			val starMaterial: Material = Material.valueOf(mongoStar.material)
			val starSize: Double = mongoStar.size

			val star = CachedStar(
				databaseId = starId,
				name = starName,
				spaceWorldName = spaceWorldName,
				location = Vec3i(starX, starY, starZ),
				material = starMaterial,
				size = starSize
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

				val planet = CachedPlanet(
					databaseId = planetId,
					name = planetName,
					sun = star,
					worldName = planetWorldName,
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
					cloudNoise = cloudNoise
				)

				planets += planet

				for (mongoMoon in Moon.getOrbiting(planetId)) {
					val moonId: Oid<Moon> = mongoMoon._id

					val moonName = mongoMoon.name
					val moonWorldName = mongoMoon.planetWorld

					val moonSize = mongoMoon.size
					val moonOrbitDistance = mongoMoon.orbitDistance
					val moonOrbitSpeed = mongoMoon.orbitSpeed
					val moonOrbitProgress = mongoMoon.orbitProgress

					val moonSeed = mongoMoon.seed

					val moonCrustMaterials: List<BlockData> = mongoMoon.crustMaterials
						.map { Material.getMaterial(it) ?: error("No material $it!") }
						.map(Material::createBlockData)

					println(moonSize)

					moons += CachedMoon(
						parent = planet,
						worldName = moonWorldName,
						databaseId = moonId,
						name = moonName,
						size = moonSize,
						orbitDistance = moonOrbitDistance,
						orbitSpeed = moonOrbitSpeed,
						orbitProgress = moonOrbitProgress,
						seed = moonSeed,
						crustMaterials = moonCrustMaterials,
						crustNoise = crustNoise
					)
				}
			}
		}

		with(planetWorldCache) { invalidateAll(); cleanUp() }
		with(planetNameCache) { invalidateAll(); cleanUp() }

		with(starNameCache) { invalidateAll(); cleanUp() }
	}

	fun getStars(): List<CachedStar> = stars

	fun getPlanets(): List<CachedPlanet> = planets

	fun getMoons(): List<CachedMoon> = moons

	fun getPlanet(planetWorld: World): CachedPlanet? = planetWorldCache[planetWorld].orElse(null)

	fun getPlanet(planetName: String): CachedPlanet? = planetNameCache[planetName].orElse(null)

	fun getStar(starName: String): CachedStar? = starNameCache[starName].orElse(null)

	fun getMoon(moonName: String): CachedMoon? = moonNameCache[moonName].orElse(null)

	override fun supportsVanilla(): Boolean {
		return true
	}
}
