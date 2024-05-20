package net.horizonsend.ion.server.features.client.networking.packets

import net.horizonsend.ion.server.features.custom.items.CustomItems as LegacyCustomItems
import net.horizonsend.ion.server.features.client.VoidNetwork
import net.horizonsend.ion.server.features.client.networking.IonPacketHandler
import net.horizonsend.ion.server.features.client.networking.Packets
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.entity.Player

object HandshakePacket : IonPacketHandler() {
	override val name = "handshake"

	override fun c2s(buf: FriendlyByteBuf, player: Player) {
		VoidNetwork.modUsers.add(player.uniqueId)

		for (id in VoidNetwork.modUsers - player.uniqueId) {
			Packets.PLAYER_ADD.send(player, id)
		}
	}

	override fun s2c(buf: FriendlyByteBuf, player: Player, vararg arguments: Any) {
		val items =
			(CustomItems.all().map { it.itemStack(1) } + LegacyCustomItems.ALL.map { it.constructItemStack() })
				.map { CraftItemStack.asNMSCopy(it) }

		buf.writeInt(items.size)

		for (i in items)
			buf.writeItem(i)
	}
}
