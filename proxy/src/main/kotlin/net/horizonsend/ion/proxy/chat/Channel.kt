package net.horizonsend.ion.proxy.chat

import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.proxy.IonProxy
import net.kyori.adventure.text.format.TextColor

interface Channel {
	val name: String
	val prefix: String?
	val displayName: String
	val commands: List<String>
	val color: TextColor
	val checkPermission: Boolean

	fun receivers(player: Player): List<Player> = IonProxy.proxy.allPlayers.toList()
	fun processMessage(player: Player, event: PlayerChatEvent): Boolean
}
