package net.horizonsend.ion.proxy.features.discord

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * A subcommand or default executor of a command
 **/
interface ExecutableCommand {
	val options: List<CommandField>

	fun execute(event: SlashCommandInteractionEvent)

	class CommandField(
		val name: String,
		private val type: OptionType,
		private val completion: String?,
		val description: String,
	) {
		fun getAutoCompletion(manager: SlashCommandManager, argument: String) = completion
			?.let { manager.getCompletions(completion, argument) }
			?.take(25)

		fun build(): OptionData {
			// No support for optional options rn
			return OptionData(type, name, description, true, completion != null)
		}
	}

	fun getField(name: String): CommandField = options.first { it.name == name }


}
