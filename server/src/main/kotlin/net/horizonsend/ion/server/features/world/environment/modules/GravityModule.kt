package net.horizonsend.ion.server.features.world.environment.modules

import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

class GravityModule(manager: WorldEnvironmentManager, val strength: Double) : EnvironmentModule(manager) {
	override fun tickSync() {
		for (player in world.players) {
			setGravity(player, strength)
		}
	}

	private fun setGravity(player: Player, value: Double) {
		var current = player.getAttribute(Attribute.GRAVITY)

		if (current == null) {
			player.registerAttribute(Attribute.GRAVITY)
			current = player.getAttribute(Attribute.GRAVITY)
		}

		val attribute = current ?: return
		attribute.baseValue = value
	}

	override fun removeEffects(player: Player) {
		setGravity(player, DEFAULT_VALUE)
	}

	companion object {
		private const val DEFAULT_VALUE = 0.08
	}
}
