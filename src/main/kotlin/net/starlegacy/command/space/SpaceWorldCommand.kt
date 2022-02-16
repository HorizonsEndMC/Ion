package net.starlegacy.command.space

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.util.green
import net.starlegacy.util.msg
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender

@CommandAlias("spaceworld")
@CommandPermission("space.spaceworld")
object SpaceWorldCommand : SLCommand() {
	@Subcommand("set")
	@CommandCompletion("@worlds true|false")
	fun onSet(sender: CommandSender, world: World, spaceWorld: Boolean) {
		SpaceWorlds.setSpaceWorld(world, spaceWorld)
		sender msg green("Set ${world.name} to space world: $spaceWorld")
		onList(sender)
	}

	@Subcommand("list")
	fun onList(sender: CommandSender) {
		sender msg "&bSpace Worlds: &d" + (Bukkit.getWorlds()
			.filter { SpaceWorlds.contains(it) }
			.takeIf { it.isNotEmpty() }
			?.joinToString { it.name }
			?: "None")
	}
}
