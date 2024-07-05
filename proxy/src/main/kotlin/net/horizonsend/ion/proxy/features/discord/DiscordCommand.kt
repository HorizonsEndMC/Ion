package net.horizonsend.ion.proxy.features.discord

import co.aikar.commands.InvalidCommandArgument
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.utils.messageEmbed
import net.horizonsend.ion.proxy.wrappers.WrappedScheduler
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.jvm.optionals.getOrNull

abstract class DiscordCommand(
	val name: String,
	val description: String
) {
	protected val log = org.slf4j.LoggerFactory.getLogger(javaClass)

	private val subcommands: MutableMap<String, DiscordSubcommand> = mutableMapOf()

	private var defaultReceiver: ExecutableCommand? = null

	open fun setup(commandManager: SlashCommandManager) {}

	protected fun registerSubcommand(subcommand: DiscordSubcommand) {
		subcommands[subcommand.name] = subcommand
	}

	/**
	 * Handle the execution of the command if no subcommand is provided
	 **/
	protected fun registerDefaultReceiver(
		options: List<ExecutableCommand.CommandField>,
		onExecute: (SlashCommandInteractionEvent) -> Unit
	): ExecutableCommand {
		val data = object : DefaultReceiver(options) {
			override fun execute(event: SlashCommandInteractionEvent) {
				onExecute.invoke(event)
			}
		}

		defaultReceiver = data

		return data
	}

	/**
	 * Handle the execution of the command if no subcommand is provided
	 **/
	protected fun registerDefaultReceiver(
		receiver: ExecutableCommand
	): ExecutableCommand {
		defaultReceiver = receiver

		return receiver
	}

	/**
	 *
	 **/
	fun getSubcommand(subcommandName: String): DiscordSubcommand? = subcommands[subcommandName]

	/**
	 *
	 **/
	fun getDefaultExecutor(): ExecutableCommand? = defaultReceiver

	protected abstract class DefaultReceiver(override val options: List<ExecutableCommand.CommandField>) : ExecutableCommand

	fun build(): SlashCommandData {
		val command = Commands.slash(name.lowercase(), description)

		command.addSubcommands(subcommands.map { (name, data) -> data.build() })

		defaultReceiver?.let { defaultReceiver ->
			val options = defaultReceiver.options

			command.addOptions(*options.map { it.build() }.toTypedArray())
		}

		return command
	}

	protected fun fail(message: () -> String): Nothing = throw InvalidCommandArgument(message())

	protected fun resolveNation(name: String): Oid<Nation> = NationCache.getByName(name)
		?: fail { "Nation $name not found" }

	protected fun resolveSettlement(name: String): Oid<Settlement> = SettlementCache.getByName(name)
		?: fail { "Settlement $name not found" }

	protected fun getPlayerName(id: SLPlayerId): String {
		return PLUGIN.server.getPlayer(id.uuid).getOrNull()?.username ?: SLPlayer.getName(id) ?: error("No such player $id")
	}

	/**
	 * Simple embed response to the command interaction
	 **/
	protected fun respondEmbed(
		event: SlashCommandInteractionEvent,
		embed: MessageEmbed,
		ephemeral: Boolean = true
	) {
		event.replyEmbeds(embed).setEphemeral(ephemeral).queue()
	}

	companion object {
		val ASYNC_COMMAND_THREAD: ExecutorService =
			Executors.newSingleThreadExecutor(WrappedScheduler.namedThreadFactory("ion-async-commands"))
	}

	/**
	 *
	 **/
	fun asyncDiscordCommand(event: SlashCommandInteractionEvent, block: () -> Unit) {
		ASYNC_COMMAND_THREAD.submit {
			try {
				block()
			} catch (e: Exception) {
				handleException(log, event, e)
			}
		}
	}

	/**
	 *
	 **/
	private fun handleException(logger: Logger, event: SlashCommandInteractionEvent, e: Throwable) {
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
