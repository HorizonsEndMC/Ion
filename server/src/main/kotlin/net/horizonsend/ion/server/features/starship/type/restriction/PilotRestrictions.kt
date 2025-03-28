package net.horizonsend.ion.server.features.starship.type.restriction

import net.horizonsend.ion.server.features.progression.Levels
import org.bukkit.entity.Player

sealed interface PilotRestrictions {
	val allowGlobalOverride: Boolean
	fun canPilot(player: Player): Boolean

	class PermissionLocked(val permissionString: String, override val allowGlobalOverride: Boolean ) : PilotRestrictions {
		override fun canPilot(player: Player): Boolean {
			return player.hasPermission(permissionString)
		}
	}

	class LevelRequirement(val minLevel: Int, val overridePermissionString: String?, override val allowGlobalOverride: Boolean = true) : PilotRestrictions {
		override fun canPilot(player: Player): Boolean {
			if (overridePermissionString != null) {
				if (player.hasPermission(overridePermissionString)) return true
			}

			return Levels[player] >= minLevel
		}
	}

	data object None : PilotRestrictions {
		override val allowGlobalOverride: Boolean = true
		override fun canPilot(player: Player): Boolean {
			return true
		}
	}
}
