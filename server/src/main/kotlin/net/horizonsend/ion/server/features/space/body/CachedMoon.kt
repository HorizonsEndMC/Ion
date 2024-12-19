package net.horizonsend.ion.server.features.space.body

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.space.Moon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.space.body.OrbitingCelestialBody.Companion.calculateOrbitLocation
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.block.data.BlockData

class CachedMoon(
	override val databaseId: Oid<Moon>,
	name: String,
	parent: CachedPlanet,
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
	calculateOrbitLocation(parent.location, orbitDistance, orbitProgress),
	parent.spaceWorldName,
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
	var parent = parent; private set

	override var orbitDistance: Int = orbitDistance; private set
	override var orbitProgress: Double = orbitProgress; private set

	override fun getParentLocation(): Vec3i {
		return parent.location
	}

	fun changeSun(newParent: CachedPlanet) {
		val world = checkNotNull(newParent.spaceWorld)
		val newLocation = calculateOrbitLocation(newParent.location, orbitDistance, orbitProgress)
		move(newLocation, world)

		parent = newParent

		Moon.setParent(databaseId, newParent.databaseId)
	}

	override fun setOrbitProgress(progress: Double) {
		val newLocation = calculateOrbitLocation(getParentLocation(), orbitDistance, progress)
		move(newLocation)

		orbitProgress = progress
		Moon.setOrbitProgress(databaseId, progress)
	}

	override fun changeOrbitDistance(newDistance: Int) {
		val newLocation = calculateOrbitLocation(parent.location, newDistance, orbitProgress)
		move(newLocation)

		orbitDistance = newDistance

		Moon.setOrbitDistance(databaseId, newDistance)
	}

	override fun orbit(updateDb: Boolean) {
		val newProgress = (orbitProgress + orbitSpeed) % 360
		val newLocation = calculateOrbitLocation(parent.location, orbitDistance, newProgress)
		move(newLocation)

		orbitProgress = newProgress

		if (updateDb) {
			Moon.setOrbitProgress(databaseId, newProgress)
		}
	}

	override fun changeDescription(newDescription: String) {
		description = newDescription

		Moon.setDescription(databaseId, newDescription)
	}

	override fun setSeed(newSeed: Long) {
		Moon.setSeed(databaseId, newSeed)
	}

	override fun setCloudMaterials(newMaterials: List<String>) {
		Moon.setCloudMaterials(databaseId, newMaterials)
	}

	override fun setCloudDensity(newDensity: Double) {
		Moon.setCloudDensity(databaseId, newDensity)
	}

	override fun setAtmosphereNoise(newNoise: Double) {
		Moon.setCloudNoise(databaseId, newNoise)
	}

	override fun setCloudThreshold(newThreshold: Double) {
		Moon.setCloudThreshold(databaseId, newThreshold)
	}

	override fun setCloudNoise(newNoise: Double) {
		Moon.setCloudNoise(databaseId, newNoise)
	}

	override fun setCrustNoise(newNoise: Double) {
		Moon.setCrustNoise(databaseId, newNoise)
	}

	override fun setCrustMaterials(newMaterials: List<String>) {
		Moon.setCrustMaterials(databaseId, newMaterials)
	}

	override fun delete() {
		Moon.delete(databaseId)
	}

	override fun formatInformation(): Component {
		return ofChildren(
			text(name, NamedTextColor.DARK_GREEN), newline(),
			text("  Parent: ", GRAY), text(parent.name, AQUA), newline(),
			text("  Space World: ", GRAY), text(spaceWorldName, AQUA), newline(),
			text("  Planet World: ", GRAY), text(enteredWorldName, AQUA), newline(),
			text("  Size: ", GRAY), text(size, AQUA), newline(),
			text("  Atmosphere Density: ", GRAY), text(cloudDensity, AQUA), newline(),
			text("  Atmosphere Radius: ", GRAY), text(atmosphereRadius, AQUA), newline(),
			text("  Atmosphere Materials: ", GRAY), text(cloudMaterials.map { it.material }.joinToString { it.toString() }, AQUA), newline(),
			text("  Crust Radius: ", GRAY), text(crustRadius, AQUA), newline(),
			text("  Crust Materials: ", GRAY), text(crustMaterials.map { it.material }.joinToString { it.toString() }, AQUA), newline(),
			text("  Description: ", GRAY), text(description, AQUA)
				.hoverEvent(text(description))
				.clickEvent(ClickEvent.copyToClipboard(description))
		)
	}
}
