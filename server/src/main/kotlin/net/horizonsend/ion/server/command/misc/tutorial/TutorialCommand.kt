package net.horizonsend.ion.server.command.misc.tutorial

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.tutorial.Tutorials
import net.horizonsend.ion.server.features.tutorial.tutorials.Tutorial
import org.bukkit.entity.Player

@CommandAlias("tutorial")
object TutorialCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(Tutorial::class.java) { c ->
			val arg = c.popFirstArg()
			Tutorials.allTutorials().find { it::class.java.simpleName == arg } ?: fail { "Tutorial type $arg not found!" }
		}

		manager.commandCompletions.registerAsyncCompletion("tutorials") {
			Tutorials.allTutorials().map { it.toString() }
		}
	}

	@Subcommand("skip")
	@CommandCompletion("tutorials")
	fun onSkipPhase(sender: Player, tutorial: Tutorial) {
		tutorial.getPhase(sender) ?: fail { "You are not currently in this tutorial" }

		sender.success("Skipping phase")
		tutorial.moveToNextStep(sender)
	}

	@Subcommand("start")
	@CommandCompletion("tutorials")
	fun onStart(sender: Player, tutorial: Tutorial) {
		sender.success("Starting tutorial ${tutorial::class.java.simpleName}")

		if (tutorial.getPhase(sender) != null) fail { "You are already in this tutorial!" }

		tutorial.startTutorial(sender)
	}

	@Subcommand("stop")
	fun onStop(sender: Player) {
		sender.success("Stopping tutorial")

		Tutorials.allTutorials().forEach { _ -> onStop(sender) }
	}
}
