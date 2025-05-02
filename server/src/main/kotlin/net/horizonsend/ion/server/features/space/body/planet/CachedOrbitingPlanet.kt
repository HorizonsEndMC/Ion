package net.horizonsend.ion.server.features.space.body.planet

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.space.Planet
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.OrbitingCelestialBody
import net.horizonsend.ion.server.features.space.body.OrbitingCelestialBody.Companion.calculateOrbitLocation
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.block.data.BlockData

class CachedOrbitingPlanet(
	override val databaseId: Oid<Planet>,
	name: String,
	sun: CachedStar,
	enteredWorldName: String,
	size: Double,
	orbitDistance: Int,
	override val orbitSpeed: Double,
	orbitProgress: Double,
	seed: Long,
	crustMaterials: List<BlockData>,
	crustNoise: Double,
	cloudDensity: Double,
	cloudMaterials: List<BlockData>,
	cloudDensityNoise: Double,
	cloudThreshold: Double,
	cloudNoise: Double,
	description: String
) : CachedPlanet(
	databaseId,
	name,
	enteredWorldName,
	calculateOrbitLocation(sun.location, orbitDistance, orbitProgress),
	sun.spaceWorldName,
	size,
	seed,
	crustMaterials,
	crustNoise,
	cloudDensity,
	cloudMaterials,
	cloudDensityNoise,
	cloudThreshold,
	cloudNoise,
	description
), OrbitingCelestialBody {
	var sun = sun; private set

	override var orbitDistance: Int = orbitDistance; private set
	override var orbitProgress: Double = orbitProgress; private set

	override fun getParentLocation(): Vec3i {
		return sun.location
	}

	fun changeSun(newSun: CachedStar) {
		val world = checkNotNull(newSun.spaceWorld)
		val newLocation = calculateOrbitLocation(newSun.location, orbitDistance, orbitProgress)
		move(newLocation, world)

		sun = newSun

		Planet.setSun(databaseId, newSun.databaseId)
	}

	override fun setOrbitProgress(progress: Double) {
		val newLocation = calculateOrbitLocation(getParentLocation(), orbitDistance, progress)
		move(newLocation)

		orbitProgress = progress
		Planet.setOrbitProgress(databaseId, progress)
	}

	override fun changeOrbitDistance(newDistance: Int) {
		val newLocation = calculateOrbitLocation(sun.location, newDistance, orbitProgress)
		move(newLocation)

		orbitDistance = newDistance

		Planet.setOrbitDistance(databaseId, newDistance)
	}

	override fun orbit(updateDb: Boolean) {
		val newProgress = (orbitProgress + orbitSpeed) % 360
		val newLocation = calculateOrbitLocation(sun.location, orbitDistance, newProgress)
		move(newLocation)

		orbitProgress = newProgress

		if (updateDb) {
			Planet.setOrbitProgress(databaseId, newProgress)
		}
	}

	override fun changeDescription(newDescription: String) {
		description = newDescription

		Planet.setDescription(databaseId, newDescription)
	}

	override fun setSeed(newSeed: Long) {
		Planet.setSeed(databaseId, newSeed)
	}

	override fun setCloudMaterials(newMaterials: List<String>) {
		Planet.setCloudMaterials(databaseId, newMaterials)
	}

	override fun setCloudDensity(newDensity: Double) {
		Planet.setCloudDensity(databaseId, newDensity)
	}

	override fun setAtmosphereNoise(newNoise: Double) {
		Planet.setCloudNoise(databaseId, newNoise)
	}

	override fun setCloudThreshold(newThreshold: Double) {
		Planet.setCloudThreshold(databaseId, newThreshold)
	}

	override fun setCloudNoise(newNoise: Double) {
		Planet.setCloudNoise(databaseId, newNoise)
	}

	override fun setCrustNoise(newNoise: Double) {
		Planet.setCrustNoise(databaseId, newNoise)
	}

	override fun setCrustMaterials(newMaterials: List<String>) {
		Planet.setCrustMaterials(databaseId, newMaterials)
	}

	override fun delete() {
		Planet.delete(databaseId)
	}

	override fun formatInformation(): Component {
		return ofChildren(
			text(name, NamedTextColor.DARK_GREEN), newline(),
			text("  Sun: ", NamedTextColor.GRAY), text(sun.name, AQUA), newline(),
			text("  Space World: ", NamedTextColor.GRAY), text(spaceWorldName, AQUA), newline(),
			text("  Planet World: ", NamedTextColor.GRAY), text(enteredWorldName, AQUA), newline(),
			text("  Size: ", NamedTextColor.GRAY), text(size, AQUA), newline(),
			text("  Atmosphere Density: ", NamedTextColor.GRAY), text(cloudDensity, AQUA), newline(),
			text("  Atmosphere Radius: ", NamedTextColor.GRAY), text(atmosphereRadius, AQUA), newline(),
			text("  Atmosphere Materials: ", NamedTextColor.GRAY), text(cloudMaterials.map { it.material }.joinToString { it.toString() }, AQUA), newline(),
			text("  Crust Radius: ", NamedTextColor.GRAY), text(crustRadius, AQUA), newline(),
			text("  Crust Materials: ", NamedTextColor.GRAY), text(crustMaterials.map { it.material }.joinToString { it.toString() }, AQUA), newline(),
			text("  Description: ", NamedTextColor.GRAY), text(description, AQUA)
				.hoverEvent(text(description))
				.clickEvent(ClickEvent.copyToClipboard(description))
		)
	}
}
