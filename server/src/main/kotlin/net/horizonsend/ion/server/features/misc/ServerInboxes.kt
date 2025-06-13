package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.utils.messages.Inboxes
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

object ServerInboxes : Inboxes() {
	override fun runAsync(task: () -> Unit) = Tasks.async(task)
}
