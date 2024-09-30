package net.horizonsend.ion.server.features.ai.starship

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.SpawningException
import net.horizonsend.ion.server.features.ai.spawning.createFromClipboard
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.slf4j.Logger
import java.io.File
import kotlin.jvm.optionals.getOrNull

@Serializable
data class StarshipTemplate(
	val schematicName: String,
	val type: StarshipType,
	val miniMessageName: String,

	val manualWeaponSets: MutableSet<AIStarshipTemplate.WeaponSet> = mutableSetOf(),
	val autoWeaponSets: MutableSet<AIStarshipTemplate.WeaponSet> = mutableSetOf(),
) {
	@Transient
	val schematicFile: File = IonServer.dataFolder.resolve("aiShips").resolve("$schematicName.schem")

	fun getSchematic(): Clipboard? = AISpawningManager.schematicCache[schematicFile].getOrNull()

	fun componentName(): Component = miniMessage.deserialize(miniMessageName)

	fun spawn(
		logger: Logger,
		location: Location,
		createController: (ActiveControlledStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	) {
		val schematic = getSchematic() ?: throw SpawningException(
			"Schematic not found for $schematicName at ${schematicFile.toURI()}",
			location.world,
			Vec3i(location)
		)

		createFromClipboard(
			logger,
			location,
			schematic,
			type,
			miniMessageName,
			createController
		) { starship ->
			callback(starship)
		}
	}
}
