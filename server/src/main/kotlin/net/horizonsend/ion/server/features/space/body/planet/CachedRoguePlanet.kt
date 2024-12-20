package net.horizonsend.ion.server.features.space.body.planet

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.space.RoguePlanet
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.block.data.BlockData

class CachedRoguePlanet(
	override val databaseId: Oid<RoguePlanet>,
	name: String,
	enteredWorldName: String,
	location: Vec3i,
	spaceWorldName: String,
	size: Double,
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
	location,
	spaceWorldName,
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
) {
	fun setLocation(location: Vec3i) {
		val (x, y, z) = location

		move(location)

		RoguePlanet.setX(databaseId, x)
		RoguePlanet.setY(databaseId, y)
		RoguePlanet.setZ(databaseId, z)
	}

	override fun changeDescription(newDescription: String) {
		description = newDescription

		RoguePlanet.setDescription(databaseId, newDescription)
	}

	override fun setSeed(newSeed: Long) {
		RoguePlanet.setSeed(databaseId, newSeed)
	}

	override fun setCloudMaterials(newMaterials: List<String>) {
		RoguePlanet.setCloudMaterials(databaseId, newMaterials)
	}

	override fun setCloudDensity(newDensity: Double) {
		RoguePlanet.setCloudDensity(databaseId, newDensity)
	}

	override fun setAtmosphereNoise(newNoise: Double) {
		RoguePlanet.setCloudNoise(databaseId, newNoise)
	}

	override fun setCloudThreshold(newThreshold: Double) {
		RoguePlanet.setCloudThreshold(databaseId, newThreshold)
	}

	override fun setCloudNoise(newNoise: Double) {
		RoguePlanet.setCloudNoise(databaseId, newNoise)
	}

	override fun setCrustNoise(newNoise: Double) {
		RoguePlanet.setCrustNoise(databaseId, newNoise)
	}

	override fun setCrustMaterials(newMaterials: List<String>) {
		RoguePlanet.setCrustMaterials(databaseId, newMaterials)
	}

	override fun delete() {
		RoguePlanet.delete(databaseId)
	}

	override fun formatInformation(): Component {
		return ofChildren(
			text(name, NamedTextColor.DARK_GREEN), newline(),
			text("  Location: ", GRAY), text(location.toString(), AQUA), newline(),
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
