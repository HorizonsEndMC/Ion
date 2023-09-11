package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

/** Registration and spawning parameters of AI ships **/
@Serializable
data class AIShipConfiguration(
	val spawnRate: Int = 20 * 60 * 15
) {
}
