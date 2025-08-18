package net.horizonsend.ion.server.features.player

import net.horizonsend.ion.common.utils.Mutes
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

object ServerMutesHook : Mutes() {
	override fun runWhenInitialized(block: () -> Unit) {
		Tasks.sync(block)
	}
}
