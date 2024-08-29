package net.horizonsend.ion.server.features.client.display.container

interface DisplayHandlerHolder {
	val displayHandler: TextDisplayHandler

	fun refresh()

	fun isValid(): Boolean

	fun removeDisplay() {
		displayHandler.remove()
	}

	fun register() {
		DisplayHandlers.displayHolders.add(this)
	}

	fun unRegister() {
		DisplayHandlers.displayHolders.remove(this)
	}
}
