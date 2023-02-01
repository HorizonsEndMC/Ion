package net.starlegacy.command.space

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.space.SpaceWorlds
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender

@CommandAlias("spaceworld")
@CommandPermission("space.spaceworld")
object SpaceWorldCommand : SLCommand() {
	@Suppress("Unused")
	@Subcommand("set")
	@CommandCompletion("@worlds true|false")
	fun onSet(sender: CommandSender, world: World, spaceWorld: Boolean) {
		SpaceWorlds.setSpaceWorld(world, spaceWorld)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Set {0} to space world: {1}", world.name, spaceWorld)
		onList(sender)
	}

	@Subcommand("list")
	fun onList(sender: CommandSender) {
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"Space Worlds: " + (
				Bukkit.getWorlds()
					.filter { SpaceWorlds.contains(it) }
					.takeIf { it.isNotEmpty() }
					?.joinToString { it.name }
					?: "None"
				)
		)
	}
}
