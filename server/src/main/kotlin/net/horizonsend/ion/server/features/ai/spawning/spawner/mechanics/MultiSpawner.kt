package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

abstract class MultiSpawner(
	private val locationProvider: Supplier<Location?>,
	private val groupMessage: Component?,
	private val individualSpawnMessage: Component?
) : SpawnerMechanic() {
	abstract fun getShips(): List<SpawnedShip>

	override suspend fun trigger(logger: Logger) {
		val ships = getShips()
		if (ships.isEmpty()) {
			debugAudience.debug("Multi spawner didn't get any ships to spawn!")
			return
		}

		val spawnOrigin = locationProvider.get()

		if (spawnOrigin == null) {
			debugAudience.debug("Location provider could not find one")
			return
		}

		val aiFleet = AIFleetManageModule.AIFleet()

		for (spawnedShip in ships) {
			val offsets = spawnedShip.offsets

			val spawnPoint = spawnOrigin.clone()

			val absoluteHeight = spawnedShip.absoluteHeight

			for (offset in offsets) {
				spawnPoint.add(offset.get())
			}

			if (absoluteHeight != null) {
				spawnPoint.y = absoluteHeight
			}

			debugAudience.debug("Spawning ${spawnedShip.template.identifier} at $spawnPoint")

			spawnedShip.spawn(logger, spawnPoint) {
				modules["fleet"] = AIFleetManageModule(this, aiFleet)
			}

			if (individualSpawnMessage != null) {
				IonServer.server.sendMessage(formatShipSpawnMessage(individualSpawnMessage, spawnedShip.template, spawnPoint.blockX, spawnPoint.blockY, spawnPoint.blockZ, spawnPoint.world.name))
			}
		}

		if (aiFleet.members.isNotEmpty() && groupMessage != null) {
			IonServer.server.sendMessage(template(
				groupMessage,
				paramColor = HEColorScheme.HE_LIGHT_GRAY,
				useQuotesAroundObjects = false,
				spawnOrigin.blockX,
				spawnOrigin.blockY,
				spawnOrigin.blockZ,
				spawnOrigin.world.name
			))
		}
	}
}
