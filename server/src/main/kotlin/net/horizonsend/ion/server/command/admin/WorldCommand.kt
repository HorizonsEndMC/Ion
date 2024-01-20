package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.World
import org.bukkit.command.CommandSender

@Subcommand("ionworld")
@CommandPermission("ion.admin.world")
object WorldCommand : SLCommand() {
	@Subcommand("flag add")
	@Suppress("unused")
	fun onAddWorldFlag(sender: CommandSender, world: World, flag: IonWorld.WorldFlag) {
		val ionWorld = IonWorld[world]

		if (ionWorld.configuration.flags.add(flag)) sender.success("Removed flag $flag")
		else sender.userError("World ${world.name} already had the flag $flag")
	}

	@Subcommand("flag remove")
	@Suppress("unused")
	fun onRemoveWorldFlag(sender: CommandSender, world: World, flag: IonWorld.WorldFlag) {
		val ionWorld = IonWorld[world]

		if (ionWorld.configuration.flags.remove(flag)) sender.success("Removed flag $flag")
		else sender.userError("World ${world.name} did not have flag $flag")
	}

	@Subcommand("flag list")
	@Suppress("unused")
	fun onListFlags(sender: CommandSender, world: World, @Optional page: Int?) {
		if ((page ?: 1) <= 0) return sender.userError("Page must not be less than or equal to zero!")

		val builder = text()

		builder.append(text("${world.name} Flags:", HE_LIGHT_GRAY, TextDecoration.BOLD), Component.newline())

		val flags = IonWorld[world].configuration.flags.toList()

		val body = formatPaginatedMenu(
			flags.size,
			"/bounty top",
			page ?: 1,
			color = NamedTextColor.RED
		) {
			val flag = flags[it]

			val remove = bracketed(text("Remove"))
				.clickEvent(ClickEvent.runCommand("/ionworld flag remove $flag"))
				.hoverEvent(text("/ionworld flag remove $flag"))

			ofChildren(flag.toComponent(HE_LIGHT_GRAY), text( ), remove)
		}

		builder.append(body)

		sender.sendMessage(builder.build())
	}
}
