package net.horizonsend.ion.server.features.starship.active.ai

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIControllers
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import java.io.File

object AIStarshipTemplates : IonServerComponent(true) {
	val templates = mutableListOf<AIStarshipTemplate>()
	val loadedSchematics = mutableMapOf<File, Clipboard>()

	override fun onEnable() {}

	val VESTA: AIStarshipTemplate = register(
		IonServer.configuration.soldShips.first().schematicFile,
		IonServer.configuration.soldShips.first().shipType,
		IonServer.configuration.soldShips.first().displayName
	) { ship -> AIControllers.dumbAI(ship) }

	data class AIStarshipTemplate(
		val schematicFile: File,
		val type: StarshipType,
		val miniMessageName: String,
		val createController: (ActiveStarship) -> AIController,
	) {
		fun schematic(): Clipboard = readSchematic(schematicFile)!!
	}

	fun register(
		schematicFile: File,
		type: StarshipType,
		miniMessageName: String,
		createController: (ActiveStarship) -> AIController
	) : AIStarshipTemplate {
		val template = AIStarshipTemplate(
			schematicFile,
			type,
			miniMessageName,
			createController
		)

		templates += template


		return template
	}
}
