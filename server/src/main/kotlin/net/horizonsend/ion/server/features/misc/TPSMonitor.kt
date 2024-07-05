package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.utils.configuration.redis
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import kotlin.math.roundToInt

object TPSMonitor : IonServerComponent() {
	private val serverName = IonServer.configuration.serverName?.lowercase() ?: "paper"

	override fun onEnable() {
		val key = "tps_$serverName"
		val tps = IonServer.server.tps[0].roundToInt().toString()

		Tasks.asyncRepeat(50L, 50L) {
			redis { set(key, tps) }
		}
	}
}
