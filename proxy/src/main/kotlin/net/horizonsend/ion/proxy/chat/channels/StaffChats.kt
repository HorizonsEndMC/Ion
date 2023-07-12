package net.horizonsend.ion.proxy.chat.channels

import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.chat.Channel
import net.kyori.adventure.text.format.NamedTextColor

class AdminChat : Channel {
	override val name = "admin"
	override val prefix = "<dark_red><bold>Admin"
	override val displayName = "<red>Admin"
	override val commands = listOf("admin", "adminchat")
	override val color = NamedTextColor.RED
	override val checkPermission = true

	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		return true
	}
}

class StaffChat : Channel {
	override val name = "staff"
	override val prefix = "<dark_gray><bold>Staff"
	override val displayName = "<aqua>Staff"
	override val commands = listOf("staff", "sc")
	override val color = NamedTextColor.LIGHT_PURPLE
	override val checkPermission = true

	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		return true
	}
}


class ModChat : Channel {
	override val name = "mod"
	override val prefix = "<dark_aqua><bold>Mod"
	override val displayName = "<aqua>Mod"
	override val commands = listOf("mod", "mc")
	override val color = NamedTextColor.AQUA
	override val checkPermission = true

	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		return true
	}
}

class DevChat : Channel {
	override val name = "dev"
	override val prefix = "<aqua><bold>Dev"
	override val displayName = "<aqua>Dev"
	override val commands = listOf("dev")
	override val color = NamedTextColor.GREEN
	override val checkPermission = true

	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		return true
	}
}

class ContentDesignChat : Channel {
	override val name = "Content-Design"
	override val prefix = "<green><bold>Content <red><bold>Design"
	override val displayName = "<green>Content <red>Design"
	override val commands = listOf("contentdesign", "cd")
	override val color = NamedTextColor.GREEN
	override val checkPermission = true

	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		return true
	}
}

class ServerChat : Channel {
	override val name = "server"
	override val prefix = "<aqua><bold>Server"
	override val displayName = "<aqua>Server"
	override val commands = listOf("serverchat")
	override val color = NamedTextColor.GREEN
	override val checkPermission = true

	override fun receivers(player: Player) = IonProxy.proxy.allPlayers.filter { it.currentServer == player.currentServer }
	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		return true
	}
}
