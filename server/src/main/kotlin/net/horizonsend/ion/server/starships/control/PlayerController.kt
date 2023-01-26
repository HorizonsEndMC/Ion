package net.horizonsend.ion.server.starships.control

import net.kyori.adventure.audience.Audience
import net.minecraft.server.level.ServerPlayer
import org.bukkit.event.player.PlayerMoveEvent

interface PlayerController : Controller {
	val serverPlayer: ServerPlayer

	fun onPlayerMoveEvent(event: PlayerMoveEvent) {}

	override fun audience(): Audience = serverPlayer.bukkitEntity
}
