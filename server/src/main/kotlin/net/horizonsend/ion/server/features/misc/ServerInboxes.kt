package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.messages.Inboxes
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component

object ServerInboxes : Inboxes() {
	override fun runAsync(task: () -> Unit) = Tasks.async(task)

	override fun notify(recipient: SLPlayerId, message: Component) {
		Notify.playerCrossServer(recipient.uuid, message)
	}
}
