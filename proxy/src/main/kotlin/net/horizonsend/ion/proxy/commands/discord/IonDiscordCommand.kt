package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.InvalidCommandArgument
import io.netty.util.internal.logging.Slf4JLoggerFactory
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.proxy.JDACommandManager
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.messageEmbed
import net.horizonsend.ion.proxy.utils.ProxyTask
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class IonDiscordCommand {
	private val log = Slf4JLoggerFactory.getInstance(this::class.java)

	companion object {
		val ASYNC_COMMAND_THREAD: ExecutorService =
			Executors.newSingleThreadExecutor(ProxyTask.namedThreadFactory("ion-async-commands"))
	}

	open fun onEnable(commandManager: JDACommandManager) {}

	protected fun fail(message: () -> String): Nothing = throw InvalidCommandArgument(message())

	protected fun resolveNation(name: String): Oid<Nation> = NationCache.getByName(name)
		?: fail { "Nation $name not found" }

	protected fun resolveSettlement(name: String): Oid<Settlement> = SettlementCache.getByName(name)
		?: fail { "Settlement $name not found" }

	protected fun respondEmbed(
		event: SlashCommandInteractionEvent,
		embed: MessageEmbed,
		ephemeral: Boolean = true
	) {
		event.replyEmbeds(embed).setEphemeral(ephemeral).queue()
	}

	protected fun getPlayerName(id: SLPlayerId): String {
		return PLUGIN.getProxy().getPlayer(id.uuid)?.name ?: SLPlayer.getName(id) ?: error("No such player $id")
	}

	protected fun asyncDiscordCommand(event: SlashCommandInteractionEvent, block: () -> Unit) {
		ASYNC_COMMAND_THREAD.submit {
			try {
				block()
			} catch (e: Exception) {
				if (e is InvalidCommandArgument) {
					event.replyEmbeds(messageEmbed(title = "Error: ${e.message}")).setEphemeral(true).queue()
					return@submit
				}

				val cause = e.cause
				if (cause is InvalidCommandArgument) {
					event.replyEmbeds(messageEmbed(title = "Error: ${e.message}")).setEphemeral(true).queue()
					return@submit
				}

				val uuid = UUID.randomUUID()
				log.error("Command Error for ${event.name}, id: $uuid", e)
				event.replyEmbeds(
					messageEmbed(
						title = "Something went wrong with that command, please tell staff.\nError ID: $uuid"
					)
				).setEphemeral(true).queue()
			}
		}
	}
}
