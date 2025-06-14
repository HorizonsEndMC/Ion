package net.horizonsend.ion.proxy.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.VelocityCommandManager
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.proxy.PLUGIN
import org.slf4j.LoggerFactory
import java.util.UUID

abstract class ProxyCommand : BaseCommand() {
	protected val log = LoggerFactory.getLogger(javaClass)
	open fun onEnable(manager: VelocityCommandManager) {}

	protected fun fail(message: () -> String): Nothing {
		throw InvalidCommandArgument(message.invoke())
	}

	protected fun <T> T.fail(message: (T) -> String) {
		throw InvalidCommandArgument(message.invoke(this))
	}

	protected fun resolveOfflinePlayer(name: String): UUID = SLPlayer.findIdByName(name)?.uuid
		?: fail { "Player $name not found. Have they joined the server?" }

	fun asyncCommand(sender: Player, block: () -> Unit) {
		PLUGIN.proxy.scheduler.async {
			try {
			    block.invoke()
			} catch (e: InvalidCommandArgument) {
				sender.userError("Error: ${e.message}")
			} catch (e: Throwable) {
				val cause = e.cause
				if (cause is InvalidCommandArgument) {
					sender.userError("Error: ${cause.message}")
					return@async
				}

				val uuid = UUID.randomUUID()
				log.error("Command Error for ${sender.username}, id: $uuid", e)
				sender.serverError("Something went wrong with that command, please tell staff.\nError ID: $uuid")
			}
		}
	}
}
