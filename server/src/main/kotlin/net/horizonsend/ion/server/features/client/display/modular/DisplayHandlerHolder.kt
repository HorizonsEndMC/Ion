package net.horizonsend.ion.server.features.client.display.modular

import org.bukkit.World

interface DisplayHandlerHolder {
	val isAlive: Boolean

	fun handlerGetWorld(): World
}
