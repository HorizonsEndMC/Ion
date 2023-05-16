package net.horizonsend.ion.server.features.client.networking.packets

import net.horizonsend.ion.server.features.client.networking.IonPacketHandler
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.entity.Player

object PlayerAdd : IonPacketHandler() {
	override val name: String = "player_add"

	override fun s2c(buf: FriendlyByteBuf, player: Player, vararg arguments: Any) {
		buf.writeUUID((arguments[0] as Player).uniqueId)
	}
}

object PlayerRemove : IonPacketHandler() {
	override val name: String = "player_remove"

	override fun s2c(buf: FriendlyByteBuf, player: Player, vararg arguments: Any) {
		buf.writeUUID((arguments[0] as Player).uniqueId)
	}
}
