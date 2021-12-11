package net.starlegacy.feature.transport

import net.starlegacy.SLComponent
import net.starlegacy.util.loadConfig

lateinit var transportConfig: TransportConfig

data class TransportConfig(
    val wires: WiresSection = WiresSection(),
    val pipes: PipesSection = PipesSection()
) {
    data class WiresSection(
        val powerUpdateRate: Long = 4L,
        val powerUpdateMaxTime: Long = 2L,
        val solarPanelPower: Int = 100,
        val maxPowerInput: Int = 1000,
        val maxShieldInput: Int = 50,
        val maxDistance: Int = 2000
    )

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
            transportConfig = loadConfig(parent = plugin.sharedDataFolder, name = "transport-settings")
        }
    }
}
