package net.horizonsend.ion.common.listeners.luckperms

import java.util.UUID
import net.horizonsend.ion.common.utilities.constructPlayerListName
import net.kyori.adventure.text.Component
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserDataRecalculateEvent

abstract class AbstractUserDataRecalculateListener {
	init {
		try {
			LuckPermsProvider.get().eventBus.subscribe(UserDataRecalculateEvent::class.java) {
				onUserDataRecalculateEvent(
					it.user.uniqueId,
					constructPlayerListName(
						it.user.username!!,
						it.user.cachedData.metaData.prefix,
						it.user.cachedData.metaData.suffix
					)
				)
			}
		} catch (_: IllegalStateException) { }
	}

	abstract fun onUserDataRecalculateEvent(uuid: UUID, playerListName: Component)
}