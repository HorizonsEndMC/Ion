package net.horizonsend.ion.server.features.client.networking.packets

import net.horizonsend.ion.server.features.client.networking.IonPacketHandler
import net.horizonsend.ion.server.features.client.networking.Packets
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

object WorldPacket : Listener, IonPacketHandler() {

	@EventHandler
	fun onChangeWorld(event: PlayerChangedWorldEvent){
		if (SpaceWorlds.contains(event.player.world)){
			Packets.WORLD_PACKET.send(event.player)
		}
	}
	override val name: String = "current_world"

	override fun s2c(buf: FriendlyByteBuf, player: Player, vararg arguments: Any) {
		buf.writeUtf(player.world.name) //send world
	}
}
