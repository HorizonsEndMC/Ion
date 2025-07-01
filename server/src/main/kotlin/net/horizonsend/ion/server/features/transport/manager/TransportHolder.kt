package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.inputs.InputManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World

interface TransportHolder {
	abstract fun getInputProvider(): InputManager

	abstract fun getWorld(): World

	open fun getGlobalCoordinate(localVec3i: Vec3i): Vec3i = localVec3i
	open fun getLocalCoordinate(globalVec3i: Vec3i): Vec3i = globalVec3i

	fun tick()
}
