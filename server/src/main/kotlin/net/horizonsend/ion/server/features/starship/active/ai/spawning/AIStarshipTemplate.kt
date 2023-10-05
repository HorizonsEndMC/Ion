package net.horizonsend.ion.server.features.starship.active.ai.spawning

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import org.apache.commons.lang.math.DoubleRange
import java.io.File

object AIStarshipTemplates : IonServerComponent(true) {
	val templates = mutableListOf<AIStarshipTemplate>()
	val loadedSchematics = mutableMapOf<File, Clipboard>()

	override fun onEnable() {}

	val VESTA: AIStarshipTemplate = register(
		"VESTA",
		IonServer.configuration.soldShips.first().schematicFile,
		IonServer.configuration.soldShips.first().shipType,
		IonServer.configuration.soldShips.first().displayName
	)

	data class AIStarshipTemplate(
		val identifier: String,
		val schematicFile: File,
		val type: StarshipType,
		val miniMessageName: String,
		val weaponsets: Set<WeaponSet> = setOf()
	) {
		fun schematic(): Clipboard = readSchematic(schematicFile)!!
	}

	fun register(
		identifier: String,
		schematicFile: File,
		type: StarshipType,
		miniMessageName: String
	) : AIStarshipTemplate {
		val template = AIStarshipTemplate(
			identifier,
			schematicFile,
			type,
			miniMessageName
		)

		templates += template


		return template
	}

	operator fun get(identifier: String) = templates.firstOrNull { it.identifier == identifier }

	data class WeaponSet(val name: String, val engagementRange: DoubleRange)
}
