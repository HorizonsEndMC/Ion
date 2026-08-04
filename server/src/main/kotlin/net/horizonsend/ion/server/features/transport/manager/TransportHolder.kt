package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.inputs.IOManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World
import org.bukkit.persistence.PersistentDataContainer
import java.util.function.Consumer

interface TransportHolder {
	fun getInputProvider(): IOManager

	fun getWorld(): World

	fun getGlobalCoordinate(localVec3i: Vec3i): Vec3i = localVec3i
	fun getLocalCoordinate(globalVec3i: Vec3i): Vec3i = globalVec3i

	fun tickExtractors()

	fun tickGraphs()

	fun storePersistentData(storeConsumer: Consumer<PersistentDataContainer>) {}
}
