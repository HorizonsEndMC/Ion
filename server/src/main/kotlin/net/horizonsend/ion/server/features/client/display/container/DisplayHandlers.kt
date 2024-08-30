package net.horizonsend.ion.server.features.client.display.container

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

object DisplayHandlers : IonServerComponent() {
	val displayHolders = mutableListOf<DisplayHandlerHolder>()

	override fun onEnable() {
		Tasks.asyncRepeat(100L, 100L, ::runUpdates)
	}

	private fun runUpdates() {
		displayHolders.removeAll {
			val toRemove = !it.isValid()

			if (toRemove) it.removeDisplay()

			toRemove
		}

		for (displayHolder in displayHolders) {
			displayHolder.refresh()
		}
	}
}
