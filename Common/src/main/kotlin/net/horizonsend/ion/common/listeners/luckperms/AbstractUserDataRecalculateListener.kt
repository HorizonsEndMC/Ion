package net.horizonsend.ion.common.listeners.luckperms

import java.util.UUID
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserDataRecalculateEvent

abstract class AbstractUserDataRecalculateListener {
	init {
		try {
			LuckPermsProvider.get().eventBus.subscribe(UserDataRecalculateEvent::class.java) {
				onUserDataRecalculateEvent(
					it.user.uniqueId,
					it.user.cachedData.metaData.prefix,
					it.user.cachedData.metaData.suffix,
				)
			}
		} catch (_: NoClassDefFoundError) { }
	}

	abstract fun onUserDataRecalculateEvent(uuid: UUID, prefix: String?, suffix: String?)
}