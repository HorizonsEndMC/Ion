package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import io.papermc.paper.util.StacktraceDeobfuscator
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.transport.old.TransportConfig
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.slf4j.Logger
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo

@CommandPermission("starlegacy.transportdebug")
@CommandAlias("transportdebug|transportbug")
object TransportDebugCommand : SLCommand() {
	@Suppress("Unused")
	@Subcommand("reload")
	fun reload(sender: CommandSender) {
		TransportConfig.reload()
		sender.success("Reloaded config")
	}

	@Subcommand("dump inputs")
	fun dumpInputs(sender: Player, type: CacheType) {
		val inputManager = sender.world.ion.inputManager
		val loc = Vec3i(sender.location)
		val inputs = inputManager.getLocations(type)
			.map { toVec3i(it) }
			.filter { it.distance(loc) < 100.0 }

		sender.highlightBlocks(inputs, 50L)
	}

	@Subcommand("force dump")
	fun forceDump(sender: Player) {
		log.error("Entire Thread Dump:")
		val threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)
		for (thread in threads) {
			dumpThread(thread, log)
		}
	}

	private fun dumpThread(thread: ThreadInfo, log: Logger) {
		log.error("------------------------------")
		//
		log.error("Current Thread: " + thread.threadName)
		log.error(
			("\tPID: " + thread.threadId
			+ " | Suspended: " + thread.isSuspended
			+ " | Native: " + thread.isInNative
			+ " | State: " + thread.threadState)
		)
		if (thread.lockedMonitors.size != 0) {
			log.error("\tThread is waiting on monitor(s):")
			for (monitor in thread.lockedMonitors) {
				log.error("\t\tLocked on:" + monitor.lockedStackFrame)
			}
		}
		log.error("\tStack:")
		//
		for (stack in StacktraceDeobfuscator.INSTANCE.deobfuscateStacktrace(thread.stackTrace))  // Paper
		{
			log.error("\t\t" + stack)
		}
	}
}
