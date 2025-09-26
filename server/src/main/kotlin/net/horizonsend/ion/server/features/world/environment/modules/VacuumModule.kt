package net.horizonsend.ion.server.features.world.environment.modules

import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.isInside
import net.horizonsend.ion.server.features.world.environment.isWearingSpaceSuit
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

class VacuumModule(manager: WorldEnvironmentManager) : EnvironmentModule(manager) {
	override fun tickSync() {
		for (player in world.players) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			checkSuffocation(player)
		}
	}
	private fun checkSuffocation(player: Player) {
		if (isWearingSpaceSuit(player)) return

		if (isInside(player.eyeLocation, 1)) return

		if (checkPressureField(player)) return

		player.damage(0.5)
	}

	private val pressureFieldPowerCooldown = PerPlayerCooldown(1, TimeUnit.SECONDS)

	private fun checkPressureField(player: Player): Boolean {
		val helmet = player.inventory.helmet ?: return false
		val customItem = helmet.customItem ?: return false

		if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) return false
		val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(helmet)
		if (!mods.contains(ItemModKeys.PRESSURE_FIELD)) return false

		val powerUsage = 10

		val power = customItem.getComponent(CustomComponentTypes.POWER_STORAGE)
		if (power.getPower(helmet) < powerUsage) return false

		pressureFieldPowerCooldown.tryExec(player) {
			power.removePower(helmet, customItem, powerUsage)
		}

		return true
	}
}
