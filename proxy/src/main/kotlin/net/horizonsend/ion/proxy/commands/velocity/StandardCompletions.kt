package net.horizonsend.ion.proxy.commands.velocity

import co.aikar.commands.VelocityCommandManager
import net.horizonsend.ion.common.database.schema.misc.SLPlayer

object StandardCompletions {
	fun registerStandardCompletions(manager: VelocityCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("allPlayers") { SLPlayer.all().map(SLPlayer::lastKnownName) }
	}
}
