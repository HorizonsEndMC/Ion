package net.horizonsend.ion.discord.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.horizonsend.ion.discord.command.annotations.CommandAlias
import net.horizonsend.ion.discord.command.annotations.Default
import net.horizonsend.ion.discord.command.annotations.Description
import net.horizonsend.ion.discord.command.annotations.ParamCompletion
import net.horizonsend.ion.discord.command.annotations.SubCommand
import net.horizonsend.ion.discord.configuration.DiscordConfiguration
import net.horizonsend.ion.discord.utils.IonDiscordScheduler
import net.horizonsend.ion.discord.utils.messageEmbed
import org.slf4j.Logger
import java.util.UUID
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaMethod

/**
 * ACF's support for JDA is outdated and probably does not support Slash Commands, so here is a horribly thrown together
 * command manager that will act similar to how ACF does.
 */
class JDACommandManager(private val jda: JDA, private val configuration: DiscordConfiguration) : ListenerAdapter() {
	private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

	private val globalCommands = mutableListOf<IonDiscordCommand>()
	private val guildCommands = mutableListOf<IonDiscordCommand>()

	private val commandCompletions: MutableMap<String, () -> List<String>> = mutableMapOf()

	fun registerGlobalCommand(command: IonDiscordCommand) {
		globalCommands.add(command)
		command.onEnable(this)
	}
	fun registerGuildCommand(command: IonDiscordCommand) {
		guildCommands.add(command)
		command.onEnable(this)
	}

	fun registerCommandCompletion(name: String, findResult: () -> List<String>) { commandCompletions[name] = findResult }

	fun build() {
		jda.updateCommands()
			.addCommands(buildCommands(globalCommands))
			.queue({ commandList ->
				log.info("Registered Global Commands: ${commandList.map { it.name }}")
			}) {
					exception ->

				log.error("Could not register global command: ${exception.message}!")
				exception.printStackTrace()
			}

		val guild = jda.getGuildById(configuration.guildID)

		if (guild == null) log.warn("Could not find guild! Cannot register guild commands.")

		guild
			?.updateCommands()
			?.addCommands(buildCommands(guildCommands))
			?.queue({ commandList ->
				log.info("Registered Guild Commands: ${commandList.map { it.name }}")
			}) {
				exception ->

				log.error("Could not register guild command: ${exception.message}!")
				exception.printStackTrace()
			}

		jda.addEventListener(this)
	}

	private fun buildCommands(commandList: List<IonDiscordCommand>): Collection<SlashCommandData> {
		return commandList.map commandMap@{ command ->
			var slashCommandData = Commands.slash(
				command::class.commandAlias ?: return@commandMap null,
				command::class.description ?: return@commandMap null
			)

			slashCommandData = slashCommandData.addSubcommands(processClassSubcommands(command::class))

			val subcommandGroups = command::class.nestedClasses.map {
				val subcommandGroupData = SubcommandGroupData(
					it::class.subcommand ?: return@map null,
					it::class.description ?: return@map null
				)

				subcommandGroupData.addSubcommands(processClassSubcommands(it::class))
			}.filterNotNull()

			slashCommandData = slashCommandData.addSubcommandGroups(subcommandGroups)

			command::class.declaredFunctions.find { it.hasAnnotation<Default>() }?.let {
				slashCommandData = slashCommandData.addOptions(processCommandMethod(it))
			}

			slashCommandData
		}.filterNotNull()
	}

	private fun processClassSubcommands(commandClass: KClass<*>): Collection<SubcommandData> =
		commandClass.declaredFunctions.map {
			val subcommandData = SubcommandData(
				it.subcommand ?: return@map null,
				it.description ?: return@map null
			)

			subcommandData.addOptions(processCommandMethod(it))
		}.filterNotNull()

	private fun processCommandMethod(commandMethod: KFunction<*>): Collection<OptionData> =
		commandMethod.parameters.map {
			if (it.type == SlashCommandInteractionEvent::class.createType()) return@map null
			if (it.kind != KParameter.Kind.VALUE) return@map null

			val optionType = when (it.type) {
				String::class.createType() -> OptionType.STRING
				else -> throw NotImplementedError("$it")
			}

			OptionData(
				optionType,										/*type*/
				it.name ?: return@map null,				/*name*/
				it.description ?: return@map null, 	/*description*/
				it.isOptional,									/*isRequired*/
				(it.completions != null)						/*isAutoComplete*/
			)
		}.filterNotNull()

	override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		val commandList = if (event.isGlobalCommand) globalCommands else guildCommands
		val commandClass = commandList.find { it::class.commandAlias == event.name }!!

		val guild = if (event.isGuildCommand) "guild" else "global"
		log.info("${event.user.name} issued $guild command: ${event.name} ${event.options.map { "${it.name}: ${it.asString}" }}")

		val method = getCommandMethod(event, commandClass)
		invokeCommand(event, commandClass, method)
	}

	fun getCommandMethod(event: CommandInteractionPayload, commandClass: IonDiscordCommand): KFunction<*> {
		return if (event.subcommandGroup != null) {
			val subcommandGroupClass = commandClass::class.nestedClasses
				.find { it.subcommand == event.subcommandGroup }!!

				subcommandGroupClass.declaredFunctions
				.find { it.subcommand == event.subcommandName }!!
		} else if (event.subcommandName != null) {
			commandClass::class.declaredFunctions
				.find { it.subcommand == event.subcommandName }!!
		} else {
			commandClass::class.declaredFunctions.find { it.hasAnnotation<Default>() }!!
		}
	}

	private val interactEventType = SlashCommandInteractionEvent::class.createType()
	private fun invokeCommand(event: SlashCommandInteractionEvent, commandClassInstance: IonDiscordCommand, commandMethod: KFunction<*>) {
		val params = listOf(
			*commandMethod.parameters.map {
				if (it.type == interactEventType) return@map event
				if (it.kind != KParameter.Kind.VALUE) return@map null

				val option = event.getOption(it.name ?: return@map null)!!

				when (it.type) {
					String::class.createType() -> option.asString
					else -> throw NotImplementedError("$it")
				}
			}.filterNotNull().toTypedArray()
		).toTypedArray()

		try { commandMethod.javaMethod!!.invoke(commandClassInstance, *params) } catch (error: Throwable) {
			handleException(log, event, error)
			return
		}
	}

	companion object {
		fun handleException(logger: Logger, event: SlashCommandInteractionEvent, e: Throwable) {
			if (e is InvalidCommandArgument) {
				event.replyEmbeds(messageEmbed(title = "Error: ${e.message}")).setEphemeral(true).queue()
				return
			}

			val cause = e.cause
			if (cause is InvalidCommandArgument) {
				event.replyEmbeds(messageEmbed(title = "Error: ${e.message}")).setEphemeral(true).queue()
				return
			}

			val uuid = UUID.randomUUID()
			logger.error("Command Error for ${event.name}, id: $uuid", e)
			event.replyEmbeds(
				messageEmbed(
					title = "Something went wrong with that command, please tell staff.\nError ID: $uuid"
				)
			).setEphemeral(true).queue()
		}
	}

	override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
		IonDiscordScheduler.async {
			val commandList = if (event.isGlobalCommand) globalCommands else guildCommands
			val commandClass = commandList.find { it::class.commandAlias == event.name }!!

			// Get the method for the command
			val method = getCommandMethod(event, commandClass)

			// Current param of the slash command
			val focusedField = event.focusedOption
			// Function param matching focused field
			val param = method.parameters.firstOrNull { it.name == focusedField.name } ?: return@async

			val completionForParam = param.completions ?: return@async
			val entered = event.focusedOption.value

			val results = commandCompletions
				.filter { completionForParam.contains(it.key) }
				.values
				.map { it() }
				.flatten()
				.filter { it.lowercase().startsWith(entered) }
				.take(25)
				.toTypedArray()

			event.replyChoiceStrings(*results).queue()
		}
	}

	private val KAnnotatedElement.commandAlias get() = findAnnotation<CommandAlias>()?.value
	private val KAnnotatedElement.description get() = findAnnotation<Description>()?.value
	private val KAnnotatedElement.subcommand get() = findAnnotation<SubCommand>()?.value
	private val KAnnotatedElement.completions get() = findAnnotation<ParamCompletion>()?.values
}
