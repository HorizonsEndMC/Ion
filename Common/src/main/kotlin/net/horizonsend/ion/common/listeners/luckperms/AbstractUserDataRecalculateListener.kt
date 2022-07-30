package net.horizonsend.ion.common.listeners.luckperms

import java.util.UUID
import net.horizonsend.ion.common.utilities.luckPerms
import net.luckperms.api.event.user.UserDataRecalculateEvent

abstract class AbstractUserDataRecalculateListener {
	init {
		luckPerms { luckPerms ->
			luckPerms.eventBus.subscribe(UserDataRecalculateEvent::class.java) {
				onUserDataRecalculateEvent(
					it.user.uniqueId,
					it.user.cachedData.metaData.prefix,
					it.user.cachedData.metaData.suffix,
				)
			}
		}
	}

	abstract fun onUserDataRecalculateEvent(uuid: UUID, prefix: String?, suffix: String?)
}