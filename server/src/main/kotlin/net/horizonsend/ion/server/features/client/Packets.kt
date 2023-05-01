package net.horizonsend.ion.server.features.client

import io.netty.buffer.Unpooled
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket
import net.minecraft.resources.ResourceLocation
import net.starlegacy.feature.misc.CustomItems
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import net.horizonsend.ion.server.features.customitems.CustomItems as NewCustomItems

enum class Packets(
	val c2s: (FriendlyByteBuf.(player: Player) -> Unit)? = null,
	val s2c: (FriendlyByteBuf.() -> Unit)? = null
) {
	HANDSHAKE(
		c2s = {
			VoidNetwork.modUsers.add(it.uniqueId)
			PLAYER_ADD.broadcast { writeUUID(it.uniqueId) }

			for (id in VoidNetwork.modUsers - it.uniqueId) {
				PLAYER_ADD.send(it) { writeUUID(it.uniqueId) }
			}
		},
		s2c = {
			val items =
				(CustomItems.all().map { it.itemStack(1) } + NewCustomItems.ALL.map { it.constructItemStack() })
					.map { CraftItemStack.asNMSCopy(it) }

			writeInt(items.size)

			for (i in items)
				writeItem(i)
		}
	),

	PLAYER_ADD(
		s2c = {}
	),

	PLAYER_REMOVE(
		s2c = {}
	);

	val id by lazy { id(name.lowercase()) }

	fun send(player: Player, add: (FriendlyByteBuf.() -> Unit)? = null) {
		check(s2c != null) { "Packet is C2S only" }

		println("Sending $id to ${player.name}")
		player.minecraft.connection.send(
			ClientboundCustomPayloadPacket(
				id,
				FriendlyByteBuf(Unpooled.buffer()).apply {
					s2c.invoke(this)
					add?.invoke(this)
				}
			)
		)
	}

	fun broadcast(add: (FriendlyByteBuf.() -> Unit)? = null) {
		println("Broadcasting $id")
		for (id in VoidNetwork.modUsers) {
			send(Bukkit.getPlayer(id)!!, add)
		}
	}

	companion object {
		fun id(s: String) = ResourceLocation("ion", s)
	}
}
