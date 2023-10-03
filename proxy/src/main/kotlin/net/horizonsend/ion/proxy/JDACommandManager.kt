package net.horizonsend.ion.proxy

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
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
import net.horizonsend.ion.proxy.commands.discord.IonDiscordCommand
import net.horizonsend.ion.proxy.commands.discord.ParamCompletion
import net.horizonsend.ion.proxy.utils.ProxyTask
import java.lang.reflect.InvocationTargetException
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
class JDACommandManager(private val jda: JDA, private val configuration: ProxyConfiguration) : ListenerAdapter() {
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
			.queue { commandList ->
				PLUGIN.slF4JLogger.info("Registered Global Commands: ${commandList.map { it.name }}")
			}

		jda.getGuildById(configuration.discordServer)
			?.updateCommands()
			?.addCommands(buildCommands(guildCommands))
			?.queue { commandList ->
				PLUGIN.slF4JLogger.info("Registered Guild Commands: ${commandList.map { it.name }}")
			}

		jda.addEventListener(this)
	}

	private fun buildCommands(commandList: List<Any>): Collection<SlashCommandData> {
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
			val message = (error as? InvocationTargetException)?.cause?.message ?: error.message

			event.replyEmbeds(messageEmbed(title = "Error: $message")).setEphemeral(true).queue()
			return
		}
	}

	override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
		ProxyTask.async {
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
				.filter { it.startsWith(entered) }
				.toTypedArray()

			event.replyChoiceStrings(*results).queue()
		}
	}

	private val KAnnotatedElement.commandAlias get() = findAnnotation<CommandAlias>()?.value
	private val KAnnotatedElement.description get() = findAnnotation<Description>()?.value
	private val KAnnotatedElement.subcommand get() = findAnnotation<Subcommand>()?.value
	private val KAnnotatedElement.completions get() = findAnnotation<ParamCompletion>()?.values
}
