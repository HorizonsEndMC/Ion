package net.horizonsend.ion.server.features.client.whereisit.mod

import io.netty.buffer.Unpooled
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.client.networking.Packets
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object ModNetworking : IonServerComponent() {
	override fun onEnable() {
		Bukkit.getMessenger().registerIncomingPluginChannel(IonServer, SearchC2S.ID.toString(), Searcher::handle)
		Bukkit.getMessenger().registerOutgoingPluginChannel(IonServer, FoundS2C.ID.toString())

		for (packet in Packets.entries) {
			log.info("Registering ${packet.id}")

			Bukkit.getMessenger().registerOutgoingPluginChannel(IonServer, packet.id.toString())
			Bukkit.getMessenger().registerIncomingPluginChannel(
				IonServer,
				packet.id.toString()
			) { s: String, player: Player, bytes: ByteArray ->
				log.info("Received message on $s by ${player.name}")
				val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))
				packet.handler.c2s(buf, player)
			}
		}
	}
}

class FoundS2C(results: Map<BlockPos, SearchResult>) : FriendlyByteBuf(Unpooled.buffer()) {
	init {
		writeInt(results.size)
		for ((key, value) in results) {
			writeBlockPos(key)
			writeEnum(value.foundType)
			val hasName = value.name != null
			writeBoolean(hasName)
			if (hasName) this.writeComponent(value.name)
		}
	}

	companion object {
		val ID = ResourceLocation("whereisit", "found_item_s2c")
		fun read(buf: FriendlyByteBuf): Map<BlockPos, SearchResult> {
			val results: MutableMap<BlockPos, SearchResult> = LinkedHashMap()
			val max = buf.readInt()
			for (i in 0 until max) {
				results[buf.readBlockPos()] = SearchResult(
					buf.readEnum(
						FoundType::class.java
					),
					if (buf.readBoolean()) buf.readComponent() else null
				)
			}
			return results
		}
	}
}

class SearchC2S(toFind: Item?, matchNbt: Boolean, nbtCompound: CompoundTag?, maximum: Int) :
	FriendlyByteBuf(Unpooled.buffer()) {
	init {
		writeResourceLocation(BuiltInRegistries.ITEM.getKey(toFind))
		writeBoolean(matchNbt)
		if (matchNbt) {
			writeNbt(nbtCompound)
		}
		writeInt(maximum)
	}

	@JvmRecord
	data class Context(val item: Item, val matchNbt: Boolean, val tag: CompoundTag?, val maximum: Int)
	companion object {
		val ID = ResourceLocation("whereisit", "find_item_c2s")
		fun read(buf: FriendlyByteBuf): Context {
			val item = BuiltInRegistries.ITEM[buf.readResourceLocation()]
			val matchNbt = buf.readBoolean()
			val tag = if (matchNbt) buf.readNbt() else null
			val maximum = buf.readInt()
			return Context(item, matchNbt, tag, maximum)
		}
	}
}
