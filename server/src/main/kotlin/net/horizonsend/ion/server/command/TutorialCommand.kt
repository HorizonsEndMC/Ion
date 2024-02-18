package net.horizonsend.ion.server.command

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.features.npcs.TutorialNPCs
import net.horizonsend.ion.server.features.tutorial.npcs.TutorialNPCType
import org.bukkit.entity.Player
import java.util.UUID

@CommandAlias("tutorialadmin")
object TutorialCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("tutorialNPCIds") {
			TutorialNPCs.manager.allNPCs().map { it.uniqueId.toString() }
		}
	}

	@Subcommand("create npc")
	fun onCreateNPC(sender: Player, type: TutorialNPCType) {
		TutorialNPCs.createNPC(
			sender.location,
			type
		)
	}
//
	@Subcommand("remove npc")
	fun onRemoveNPC(sender: Player, uuid: String) {
		TutorialNPCs.editStorage {
			types.remove(UUID.fromString(uuid))
		}
	}

	@Subcommand("reload npc")
	fun onReloadNPC(sender: Player) {
		TutorialNPCs.onDisable()
		TutorialNPCs.onEnable()
	}
}
