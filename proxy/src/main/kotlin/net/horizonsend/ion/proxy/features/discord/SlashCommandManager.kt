package net.horizonsend.ion.proxy.features.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.horizonsend.ion.common.utils.discord.DiscordConfiguration
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.utils.messageEmbed
import net.kyori.adventure.text.format.NamedTextColor

class SlashCommandManager(private val jda: JDA, private val configuration: DiscordConfiguration) : ListenerAdapter() {
	private val log = org.slf4j.LoggerFactory.getLogger("Ion-SlashCommandManager")

	private val guildCommands: MutableMap<String, DiscordCommand> = mutableMapOf()
	private val globalCommands: MutableMap<String, DiscordCommand> = mutableMapOf()

	private val commandCompletions: MutableMap<String, (String) -> Collection<String>> = mutableMapOf()

	fun registerGuildCommand(command: DiscordCommand) {
		guildCommands[command.name] = command

		command.setup(this)
	}

	fun registerGlobalCommand(command: DiscordCommand) {
		globalCommands[command.name] = command

		command.setup(this)
	}

	fun registerCompletion(identifier: String, completion: (String) -> Collection<String>) {
		commandCompletions[identifier] = completion
	}

	fun getCompletions(identifier: String, argument: String): Collection<String> = commandCompletions[identifier]!!.invoke(argument)

	fun build() {
		jda.retrieveCommands()
			.complete()
			.forEach { command ->
				command.delete().complete()
				log.info("Cleared old global command: ${command.name}")
			}

		jda.getGuildById(configuration.guild)?.let { guild ->
			guild.retrieveCommands()
				.complete()
				.forEach {
					it.delete().complete()
					log.info("Cleared old guild command: ${it.name}")
				}
		}

		updateGlobalCommands()
		updateGuildCommands()

		jda.addEventListener(this)
	}

	private fun updateGlobalCommands() {
		for ((_, command) in globalCommands) {
			jda.upsertCommand(command.build()).queue({ commandList ->
				log.info("Registered Guild Command: ${commandList.name}")
			}) { exception ->

				log.error("Could not register guild command: ${exception.message}!")
				exception.printStackTrace()
			}
		}
	}

	private fun updateGuildCommands() {
		val guild = jda.getGuildById(configuration.guild)

		if (guild == null) {
			log.warn("Could not find guild! Cannot register guild commands.")
			return
		}

		for ((_, command) in guildCommands) {
			guild.upsertCommand(command.build()).queue({ commandList ->
				log.info("Registered Guild Command: ${commandList.name}")
			}) { exception ->

				log.error("Could not register guild command: ${exception.message}!")
				exception.printStackTrace()
			}
		}
	}

	override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		log.info("${event.user.name} issued ${if (event.isGuildCommand) "guild" else "global"}" +
				" command: ${event.fullCommandName} ${event.options.map { "${it.name}: ${it.asString}" }}")

		event.deferReply(true)

		val executor = getCommand(event)

		if (executor == null) {
			event.replyEmbeds(messageEmbed(
				title = "Looks like that command got lost, please open a bug report.",
				color = NamedTextColor.RED.value()
			)).setEphemeral(true).queue()

			return
		}

		executor.execute(event)
	}

	override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) = PLUGIN.proxy.scheduler.async {
		val executor = getCommand(event)

		if (executor == null) {
			log.error("NO EXECUTOR FOR COMPLETION! ${event.fullCommandName}")

			event.replyChoiceStrings().queue()
			return@async
		}

		val focusedField = event.focusedOption.name

		val commandField = executor.getField(focusedField)

		val results = commandField.getAutoCompletion(this, event.focusedOption.value) ?: listOf()

		event.replyChoiceStrings(results).queue()
	}

	fun getCommand(payload: CommandInteractionPayload): ExecutableCommand? {
		val commandName = payload.name

		val discordCommand = if (payload.isGuildCommand) guildCommands[commandName] else globalCommands[commandName]

		discordCommand ?: return null

		payload.subcommandGroup?.let { throw NotImplementedError("") }

		val subCommandName = payload.subcommandName

		if (subCommandName != null) {
			// If there is a subcommand used, there should be a corresponding registered subcommand
			return discordCommand.getSubcommand(subCommandName)
		}

		// If no subcommands, use the default. If there is no default and no subcommands, someone messed up.
		return discordCommand.getDefaultExecutor()
	}
}
