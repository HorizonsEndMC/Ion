package net.horizonsend.ion.server.features.transport

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles.sharedDataFolder

lateinit var transportConfig: TransportConfig

@Serializable
data class TransportConfig(
	val wires: WiresSection = WiresSection(),
	val pipes: PipesSection = PipesSection()
) {
	@Serializable
	data class WiresSection(
		val powerUpdateRate: Long = 4L,
		val powerUpdateMaxTime: Long = 2L,
		val solarPanelPower: Int = 100,
		val maxPowerInput: Int = 1000,
		val maxShieldInput: Int = 50,
		val maxDistance: Int = 2000,
		val solarTickInterval: Int = 10
	)

	@Serializable
	data class PipesSection(
		val inventoryCheckInterval: Long = 4L,
		val inventoryCheckMaxTime: Long = 2L,
		val maxInventoryChecks: Int = 1000,
		val maxDistance: Int = 2000
	)

	companion object : IonServerComponent() {
		override fun onEnable() {
			reload()
		}

		fun reload() {
			transportConfig = Configuration.load(sharedDataFolder, "transport-settings.json")
		}
	}
}
