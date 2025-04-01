package net.horizonsend.ion.server.features.starship.type.restriction

import net.horizonsend.ion.common.utils.text.MiniMessageString
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.progression.Levels
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

sealed interface PilotRestrictions {
	val allowGlobalOverride: Boolean
	fun canPilot(player: Player): PilotResult

	class PermissionLocked(val permissionString: String, override val allowGlobalOverride: Boolean ) : PilotRestrictions {
		override fun canPilot(player: Player): PilotResult {
			return if (player.hasPermission(permissionString)) PilotResult.Success else PilotResult.Failure("<red>You don't have permission to use that starship class!")
		}
	}

	class LevelRequirement(val minLevel: Int, val overridePermissionString: String?, override val allowGlobalOverride: Boolean = true) : PilotRestrictions {
		override fun canPilot(player: Player): PilotResult {
			if (overridePermissionString != null) {
				if (player.hasPermission(overridePermissionString)) return PilotResult.Success
			}

			return if (Levels[player] >= minLevel) PilotResult.Success else PilotResult.Failure("<red>You are not a high enough level to pilot that starship class!")
		}
	}

	data object None : PilotRestrictions {
		override val allowGlobalOverride: Boolean = true
		override fun canPilot(player: Player): PilotResult {
			return PilotResult.Success
		}
	}

	sealed interface PilotResult {
		val success: Boolean

		data object Success : PilotResult {
			override val success: Boolean = true
		}

		data class Failure(val reason: MiniMessageString) : PilotResult {
			override val success: Boolean = true

			fun asComponent(): Component = reason.miniMessage()
		}
	}
}
