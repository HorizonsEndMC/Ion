package net.horizonsend.ion.common.listeners.luckperms

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.node.NodeMutateEvent

abstract class AbstractNodeMutateListener {
	init {
		try {
			LuckPermsProvider.get().eventBus.subscribe(NodeMutateEvent::class.java) {
				onNodeMutateEvent()
			}
		} catch (_: IllegalStateException) { }
	}

	abstract fun onNodeMutateEvent()
}