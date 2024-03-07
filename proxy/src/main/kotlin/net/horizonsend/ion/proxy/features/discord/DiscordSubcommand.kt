package net.horizonsend.ion.proxy.features.discord

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

abstract class DiscordSubcommand(
	val name: String,
	val description: String,
	override val options: List<ExecutableCommand.CommandField>
) : ExecutableCommand {
	fun build(): SubcommandData {
		val subcommand = SubcommandData(name, description)

		subcommand.addOptions(*options.map { it.build() }.toTypedArray())

		return subcommand
	}

	companion object {
		fun DiscordCommand.subcommand(
			name: String,
			description: String,
			options: List<ExecutableCommand.CommandField>,
			onExecute: (SlashCommandInteractionEvent) -> Unit
		): DiscordSubcommand = object : DiscordSubcommand(name, description, options) {
			override fun execute(event: SlashCommandInteractionEvent) = asyncDiscordCommand(event) {
				onExecute.invoke(event)
			}
		}
	}
}
