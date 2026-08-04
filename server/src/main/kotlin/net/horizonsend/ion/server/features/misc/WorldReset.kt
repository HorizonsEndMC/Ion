package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import java.nio.file.Paths
import java.time.LocalDate

object WorldReset : IonServerComponent() {
	fun onStartup() {
		if (!AutoRestart.isRestart) return
		if (LocalDate.now().dayOfWeek != ConfigurationFiles.serverConfiguration().worldResetSettings.worldResetDay) return

		ConfigurationFiles.serverConfiguration().worldResetSettings.worldResetDirectories.forEach {
			val file = Paths.get(IonServer.dataFolder.absolutePath + it).normalize().toFile()

			log.info("Deleting ${file.absolutePath}")
			file.deleteRecursively()
		}
	}
}
