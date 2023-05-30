package net.horizonsend.ion.server.features.client.networking.packets

import net.horizonsend.ion.server.features.client.networking.IonPacketHandler
import net.horizonsend.ion.server.features.client.networking.Packets
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object GetPosPacket : IonPacketHandler() {
	override val name = "getpos"

	override fun c2s(buf: FriendlyByteBuf, player: Player) {
		val name = buf.readUtf()
		val coords = Bukkit.getPlayer(name)?.location ?: return

		Packets.GETPOS.send(
			player,

			name,
			coords.world.name, // world
			coords.blockX, // x
			coords.blockY, // y
			coords.blockZ, // z
			((coords.yaw + 360) % 360).toDouble(), // yaw
			coords.pitch // pitch
		)
	}

	override fun s2c(buf: FriendlyByteBuf, player: Player, vararg arguments: Any) {
		buf.writeUtf(arguments[0] as String)

		buf.writeUtf(arguments[1] as String)
		buf.writeInt(arguments[2] as Int)
		buf.writeInt(arguments[3] as Int)
		buf.writeInt(arguments[4] as Int)
		buf.writeDouble(arguments[5] as Double)
		buf.writeFloat(arguments[6] as Float)
	}
}
