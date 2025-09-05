package net.horizonsend.ion.server.features.client.display.modular

import net.minecraft.world.entity.Display
import org.bukkit.util.Vector

interface DisplayWrapper {
	fun getEntity(): Display

	var position: Vector
	var heading: Vector
	var scale: Vector
	var offset: Vector

	fun update()
	fun remove()
}
