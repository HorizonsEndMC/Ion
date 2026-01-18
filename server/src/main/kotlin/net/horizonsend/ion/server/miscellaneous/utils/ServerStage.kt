package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import java.util.concurrent.TimeUnit

object ServerStage : IonComponent() {
	fun getServerStage(): Int {
		val configuration = ConfigurationFiles.serverConfiguration.get()
		val startTime = configuration.serverStartDate ?: return 0
		val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
		val stage = when {
			currentTime - startTime < TimeUnit.DAYS.toSeconds(1) -> 1
			currentTime - startTime <= TimeUnit.DAYS.toSeconds(5) && currentTime - startTime > TimeUnit.DAYS.toNanos(1) -> 2
			currentTime - startTime <= TimeUnit.DAYS.toSeconds(12) && currentTime - startTime > TimeUnit.DAYS.toNanos(5) -> 3
			currentTime - startTime >= TimeUnit.DAYS.toSeconds(19) -> 4

			else -> 0
		}
		return stage
	}
}
