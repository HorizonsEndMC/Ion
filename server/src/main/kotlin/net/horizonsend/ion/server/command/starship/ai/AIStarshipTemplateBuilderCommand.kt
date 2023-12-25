package net.horizonsend.ion.server.command.starship.ai

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.StarshipType
import org.bukkit.entity.Player

@CommandPermission("ion.aidebug")
@CommandAlias("aitemplatebuilder")
@Description("A series of commands to simplify the construction of ai starship templates")
object AIStarshipTemplateBuilderCommand : SLCommand() {
	private val currentBuilding = mutableMapOf<String, AISpawningConfiguration.AIStarshipTemplate>()

	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("buildingTemplates") { currentBuilding.keys }
		manager.commandCompletions.registerAsyncCompletion("existingTemplates") { IonServer.aiSpawningConfiguration.templates.map { it.identifier } }
	}

	@Subcommand("build from new")
	@Description("Start construction of a template")
	@Suppress("unused")
	fun onStartNew(sender: Player, identifier: String, schematicName: String, type: StarshipType) {
		val identifierUppercase = identifier.uppercase()

		currentBuilding[identifierUppercase] = AISpawningConfiguration.AIStarshipTemplate(
			identifier = identifierUppercase,
			schematicName = schematicName,
			type = type,
		)

		sender.success("Created template $identifierUppercase")
	}

	@Subcommand("build from existing")
	@Description("Start construction of a template")
	@CommandCompletion("@existingTemplates")
	@Suppress("unused")
	fun onStartFromExisting(sender: Player, identifier: String) {
		val identifierUppercase = identifier.uppercase()

		currentBuilding[identifierUppercase] = IonServer.aiSpawningConfiguration.templates.firstOrNull { it.identifier == identifierUppercase } ?: fail {
			"Could not find template $identifierUppercase"
		}

		sender.success("Created builder from template $identifierUppercase")
	}

	@Subcommand("list autosets")
	@Description("List auto weapon sets for the template")
	@CommandCompletion("@buildingTemplates @nothing")
	@Suppress("unused")
	fun listAutoWeaponSets(sender: Player, identifier: String) {
		val template = requireBuilding(identifier)

		sender.information(template.autoWeaponSets.joinToString { it.toString() })
	}

	@Subcommand("list manualsets")
	@Description("List manual weapon sets for the template")
	@CommandCompletion("@buildingTemplates @nothing")
	@Suppress("unused")
	fun listManualWeaponSets(sender: Player, identifier: String) {
		val template = requireBuilding(identifier)

		sender.information(template.autoWeaponSets.joinToString { it.toString() })
	}

	@Subcommand("add manualset")
	@Description("Add manual weapon sets to the template")
	@CommandCompletion("@buildingTemplates name minDistance maxDistance")
	@Suppress("unused")
	fun addManualSet(sender: Player, identifier: String, name: String, minDistance: Double, maxDistance: Double) {
		val template = requireBuilding(identifier)

		val set = AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name, minDistance, maxDistance)

		template.manualWeaponSets.add(set)
		sender.success("Added manual weapon set $set to $identifier")
	}

	@Subcommand("add autoset")
	@Description("Add auto weapon sets to the template")
	@CommandCompletion("@buildingTemplates name minDistance maxDistance")
	@Suppress("unused")
	fun addAutoSet(sender: Player, identifier: String, name: String, minDistance: Double, maxDistance: Double) {
		val template = requireBuilding(identifier)

		val set = AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name, minDistance, maxDistance)

		template.manualWeaponSets.add(set)
		sender.success("Added auto weapon set $set to $identifier")
	}

	@Subcommand("set schematicname")
	@Description("Set schematic name")
	@CommandCompletion("@buildingTemplates @nothing")
	@Suppress("unused")
	fun setSchematicName(sender: Player, identifier: String, name: String) {
		val template = requireBuilding(identifier)
		val withoutExtension = name.removeSuffix(".schem")

		template.schematicName = withoutExtension
		sender.success("Set schematic of $identifier to $withoutExtension")
	}

	@Subcommand("set displayname")
	@Description("Set display name (minimessage format)")
	@CommandCompletion("@buildingTemplates @nothing")
	@Suppress("unused")
	fun setDisplayName(sender: Player, identifier: String, name: String) {
		val template = requireBuilding(identifier)

		template.miniMessageName = name
		sender.success("Set display name of $identifier to $name")
	}

	@Subcommand("set controller")
	@Description("Set controller")
	@CommandCompletion("@buildingTemplates @controllerFactories")
	@Suppress("unused")
	fun setController(sender: Player, identifier: String, controllerFactory: String) {
		val template = requireBuilding(identifier)

		template.controllerFactory = controllerFactory
		sender.success("Set schematic of $identifier to $controllerFactory")
	}

	@Subcommand("set type")
	@Description("Set starship type")
	@CommandCompletion("@buildingTemplates")
	@Suppress("unused")
	fun setType(sender: Player, identifier: String, type: StarshipType) {
		val template = requireBuilding(identifier)

		template.type = type
		sender.success("Set display name of $identifier to $type")
	}

	@CommandCompletion("@buildingTemplates")
	@Subcommand("build")
	@Suppress("unused")
	fun onBuild(sender: Player, identifier: String, @Optional overwrite: String?) {
		val identifierUppercase = identifier.uppercase()
		val template = requireBuilding(identifier)

		// Handle overwrites
		if (IonServer.aiSpawningConfiguration.templates.any { it.identifier == identifierUppercase }) {
			if (overwrite == "confirm") {
				sender.alert("A template with the identifier $identifierUppercase already exists! If you wish to overwrite it, use /aitemplatebuilder build $identifierUppercase confirm")
				return
			} else {
				// They confirmed
				IonServer.aiSpawningConfiguration.templates.removeAll { it.identifier == identifierUppercase }
			}
		}

		IonServer.aiSpawningConfiguration.templates.add(template)

		Configuration.save(IonServer.aiSpawningConfiguration, IonServer.configurationFolder, "aiships.json")
	}

	private fun requireBuilding(identifier: String): AISpawningConfiguration.AIStarshipTemplate =
		currentBuilding[identifier.uppercase()] ?: fail { "Could not find template ${identifier.uppercase()}" }
}
