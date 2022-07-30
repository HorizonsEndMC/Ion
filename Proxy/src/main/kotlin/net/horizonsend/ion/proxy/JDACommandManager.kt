package net.horizonsend.ion.proxy

import co.aikar.commands.annotation.Default
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.horizonsend.ion.proxy.annotations.CommandMeta
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * ACF's support for JDA is outdated and probably does not support Slash Commands, so here is a horribly thrown together
 * command manager that will act similar to how ACF does.
 */
class JDACommandManager(jda: JDA, private val commandClasses: List<Any>) : ListenerAdapter() {
	constructor(jda: JDA, vararg commandClass: Any) : this(jda, commandClass.toList())

	init {
		jda.updateCommands().addCommands(commandClasses.map { commandClass ->
			var commandData = Commands.slash(
				commandClass::class.java.commandMeta.name,
				commandClass::class.java.commandMeta.description
			)

			val subcommands = processClassSubcommands(commandClass::class.java)

			commandData = commandData.addSubcommands(subcommands)

			val subcommandGroups = commandClass::class.java.classes
				.filter { it.hasCommandMeta }
				.map {
					SubcommandGroupData(it.commandMeta.name, it.commandMeta.description)
						.addSubcommands(processClassSubcommands(it))
				}

			commandData = commandData.addSubcommandGroups(subcommandGroups)

			if (subcommands.isEmpty() && subcommandGroups.isEmpty()) {
				commandClass::class.java.defaultCommand?.let {
					commandData = commandData.addOptions(processCommandMethod(it))
				}
			} else if (commandClass::class.java.defaultCommand != null) {
				throw IllegalArgumentException(
					"Command containing subcommands or subcommand groups can not have a default command."
				)
			}

			commandData
		}).queue()

		jda.addEventListener(this)
	}

	private fun <T> processClassSubcommands(commandClass: Class<T>): Collection<SubcommandData> =
		commandClass.methods
			.filter { it.hasCommandMeta }
			.map {
				SubcommandData(it.commandMeta.name, it.commandMeta.description)
					.addOptions(processCommandMethod(it))
			}

	private fun processCommandMethod(commandMethod: Method): Collection<OptionData> {
		val optionData = mutableListOf<OptionData>()

		for (parameter in commandMethod.parameters) {
			if (parameter.type == SlashCommandInteractionEvent::class.java) continue

			val parameterMetadata = parameter.commandMeta

			val optionType = when (parameter.type) {
				String::class.java -> OptionType.STRING
				Int::class.java -> OptionType.INTEGER
				Boolean::class.java -> OptionType.BOOLEAN
				Member::class.java, User::class.java -> OptionType.USER
				Channel::class.java -> OptionType.CHANNEL
				Role::class.java -> OptionType.ROLE
				IMentionable::class.java -> OptionType.MENTIONABLE
				Double::class.java,
				Long::class.java -> OptionType.NUMBER

				Attachment::class.java -> OptionType.ATTACHMENT
				else -> throw NotImplementedError("Parameter type ${parameter.type.simpleName} is not supported by JDA.")
			}

			optionData.add(
				OptionData(
					optionType,
					parameterMetadata.name,
					parameterMetadata.description,
					!parameter.isAnnotationPresent(Nullable::class.java) || parameter.isAnnotationPresent(NotNull::class.java)
				)
			)
		}

		return optionData
	}

	private inline val <T> Class<T>.defaultCommand
		get() =
			methods.filter { it.isAnnotationPresent(Default::class.java) }.getOrNull(0)

	private inline val AnnotatedElement.hasCommandMeta
		get() =
			isAnnotationPresent(CommandMeta::class.java)

	private inline val Parameter.commandMeta
		get() =
			getAnnotation(CommandMeta::class.java) ?: throw Exception("Missing CommandMeta annotation on $name.")

	private inline val Method.commandMeta
		get() =
			getAnnotation(CommandMeta::class.java) ?: throw Exception("Missing CommandMeta annotation on $name.")

	private inline val <T> Class<T>.commandMeta
		get() =
			getAnnotation(CommandMeta::class.java) ?: throw Exception("Missing CommandMeta annotation on $simpleName.")

	override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		commandClasses
			.find { it::class.java.commandMeta.name == event.name }
			?.let { commandClass ->
				if (event.subcommandGroup != null) {
					val subcommandGroupClass = commandClass::class.java.classes
						.filter { it.hasCommandMeta }
						.find { it.commandMeta.name == event.subcommandGroup }

					subcommandGroupClass?.methods
						?.filter { it.hasCommandMeta }
						?.find { it.commandMeta.name == event.subcommandName }
						?.let {
							invokeCommand(
								event,
								subcommandGroupClass.getConstructor(commandClass::class.java).newInstance(commandClass),
								it
							)
						}

				} else if (event.subcommandName != null) {
					commandClass::class.java.methods
						.filter { it.hasCommandMeta }
						.find { it.commandMeta.name == event.subcommandName }
						?.let { invokeCommand(event, commandClass, it) }

				} else {
					commandClass::class.java.defaultCommand?.let { invokeCommand(event, commandClass, it) }
				}
			}
	}

	private fun invokeCommand(event: SlashCommandInteractionEvent, commandClass: Any, commandMethod: Method) {
		commandMethod.invoke(commandClass, *commandMethod.parameters.map {
			if (it.type == SlashCommandInteractionEvent::class.java) return@map event

			val option = event.getOption(it.commandMeta.name)!!

			when (it.type) {
				Attachment::class.java -> option.asAttachment
				String::class.java -> option.asString
				Boolean::class.java -> option.asBoolean
				Long::class.java -> option.asLong
				Int::class.java -> option.asInt
				Double::class.java -> option.asDouble
				IMentionable::class.java -> option.asMentionable
				Member::class.java -> option.asMember
				User::class.java -> option.asUser
				Role::class.java -> option.asRole
				Channel::class.java -> option.asChannel
				else -> throw NotImplementedError("Parameter type ${it.type.simpleName} is not supported by JDA.")
			}
		}.toTypedArray())
	}
}