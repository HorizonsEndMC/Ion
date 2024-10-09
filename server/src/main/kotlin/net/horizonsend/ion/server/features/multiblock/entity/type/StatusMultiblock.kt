package net.horizonsend.ion.server.features.multiblock.entity.type

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty

interface StatusMultiblock {
	val statusManager: StatusManager

	fun setStatus(status: Component) {
		statusManager.setStatus(status)
	}

	class StatusManager {
		val updateManager = mutableSetOf<Runnable>()

		var status: Component = empty(); private set

		fun setStatus(status: Component) {
			this.status = status
			updateManager.forEach { it.run() }
		}
	}
}
