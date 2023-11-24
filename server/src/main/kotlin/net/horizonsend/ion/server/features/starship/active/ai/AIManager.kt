package net.horizonsend.ion.server.features.starship.active.ai

import net.horizonsend.ion.server.IonServerComponent

object AIManager : IonServerComponent() {
	val serviceExecutor = AIServiceExecutor()

	override fun onEnable() {
		serviceExecutor.initialize()
	}

	override fun onDisable() {
		serviceExecutor.shutDown()
	}
}
