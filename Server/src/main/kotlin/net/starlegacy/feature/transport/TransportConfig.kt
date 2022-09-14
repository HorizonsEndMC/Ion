package net.starlegacy.feature.transport

import net.starlegacy.SLComponent
import net.starlegacy.sharedDataFolder
import net.starlegacy.util.loadConfig

lateinit var transportConfig: TransportConfig

data class TransportConfig(
	val pipes: PipesSection = PipesSection()
) {
	data class PipesSection(
		val inventoryCheckInterval: Long = 4L,
		val inventoryCheckMaxTime: Long = 2L,
		val maxInventoryChecks: Int = 1000,
		val maxDistance: Int = 2000
	)

	companion object : SLComponent() {
		override fun onEnable() {
			reload()
		}

		fun reload() {
			transportConfig = loadConfig(parent = sharedDataFolder, name = "transport-settings")
		}
	}
}