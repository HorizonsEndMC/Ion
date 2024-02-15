package net.horizonsend.ion.server.command

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.npcs.TutorialNPCs
import net.horizonsend.ion.server.features.tutorial.npcs.TutorialNPCType
import org.bukkit.entity.Player
import java.util.UUID

@CommandAlias("tutorialadmin")
object TutorialCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("tutorialNPCIds") {
			TutorialNPCs.store.storage.npcs.map { it.uuid.toString() }
		}
	}

	@Subcommand("admin create npc")
	fun onCreateNPC(sender: Player, type: TutorialNPCType) {
		TutorialNPCs.store.storage.npcs.add(TutorialNPCs.TutorialDroid(
			ServerConfiguration.Pos(
				sender.location.world.name,
				sender.location.x.toInt(),
				sender.location.y.toInt(),
				sender.location.z.toInt(),),
			UUID.randomUUID(),
			type
		))

		TutorialNPCs.store.saveStorage()
	}

	@Subcommand("admin remove npc")
	fun onRemoveNPC(sender: Player, uuid: String) {
		TutorialNPCs.store.storage.npcs.removeAll { it.uuid == UUID.fromString(uuid) }

		TutorialNPCs.store.saveStorage()
	}

	@Subcommand("admin reload npc")
	fun onReloadNPC(sender: Player) {
		TutorialNPCs.onDisable()
		TutorialNPCs.onEnable()
	}
}
