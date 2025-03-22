package net.horizonsend.ion.server.features.starship.type.restriction

import net.horizonsend.ion.server.features.progression.Levels
import org.bukkit.entity.Player

sealed interface PilotRestrictions {
	val allowOverridePermission: Boolean
	fun canPilot(player: Player): Boolean

	class PermissionLocked(val permissionString: String, override val allowOverridePermission: Boolean ) : PilotRestrictions {
		override fun canPilot(player: Player): Boolean {
			return player.hasPermission(permissionString)
		}
	}

	class LevelRequirement(val minLevel: Int, val overridePermissionString: String?, override val allowOverridePermission: Boolean = false) : PilotRestrictions {
		override fun canPilot(player: Player): Boolean {
			if (overridePermissionString != null) {
				if (player.hasPermission(overridePermissionString)) return true
			}

			return Levels[player] >= minLevel
		}
	}

	data object None : PilotRestrictions {
		override val allowOverridePermission: Boolean = true
		override fun canPilot(player: Player): Boolean {
			return true
		}
	}
}
