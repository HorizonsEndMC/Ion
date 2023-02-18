package net.horizonsend.ion.server.features.whereisit.mod

import io.netty.buffer.Unpooled
import net.horizonsend.ion.server.miscellaneous.extensions.information
import net.horizonsend.ion.server.miscellaneous.handle
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.Nameable
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.LecternBlock
import net.minecraft.world.level.block.entity.LecternBlockEntity
import net.starlegacy.listener.misc.ProtectionListener
import net.starlegacy.util.Tasks
import net.starlegacy.util.toLocation
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.entity.Player

object Searcher {
	fun handle(s: String, player: Player, buf: ByteArray) = Tasks.async {
		val searchContext = SearchC2S.read(FriendlyByteBuf(Unpooled.wrappedBuffer(buf)))
		val itemToFind = searchContext.item
		if (itemToFind == Items.AIR) return@async

		val basePos = player.handle.blockPosition()
		val world = player.world.handle
		val positions =
			searchWorld(
				basePos,
				world,
				itemToFind,
				searchContext.tag
			).filterNot {
				ProtectionListener.denyBlockAccess(
					player,
					it.key.toLocation(player.world)
				)
			}

		if (positions.isNotEmpty()) {
			val packet = FoundS2C(positions)

			player.handle.connection.send(
				ClientboundCustomPayloadPacket(FoundS2C.ID, packet)
			)
		} else {
			player.information("Item not found")
		}
	}

	fun searchItemStack(player: Player, toFind: org.bukkit.inventory.ItemStack): Map<BlockPos, SearchResult> {
		val item = CraftItemStack.asNMSCopy(toFind)
		val itemToFind = item.item
		val tag = item.tag

		val basePos = player.handle.blockPosition()
		val world = player.world.handle

		return searchWorld(
			basePos,
			world,
			itemToFind,
			tag
		).filterNot {
			ProtectionListener.denyBlockAccess(
				player,
				it.key.toLocation(player.world)
			)
		}
	}

	fun searchWorld(
		playerPos: BlockPos,
		world: ServerLevel,
		toFind: Item,
		toFindTag: CompoundTag?
	): Map<BlockPos, SearchResult> {
		val positions: MutableMap<BlockPos, SearchResult> = HashMap()
		val radius = 20 // TODO config
		var checkedBECount = 0
		val minChunkX = -radius + playerPos.x shr 4
		val maxChunkX = radius + 1 + playerPos.x shr 4
		val minChunkZ = -radius + playerPos.z shr 4
		val maxChunkZ = radius + 1 + playerPos.z shr 4
		for (chunkX in minChunkX..maxChunkX) {
			for (chunkZ in minChunkZ..maxChunkZ) {
				val chunk = world.getChunk(chunkX, chunkZ)
				checkedBECount += chunk.getBlockEntities().size
				for ((pos, be) in chunk.getBlockEntities()) {
					if (pos.closerThan(playerPos, radius.toDouble())) {
						val state = chunk.getBlockState(pos)
						var foundType = FoundType.NOT_FOUND
						var invName: Component? = null
						if (be is Nameable && be.hasCustomName()) invName = be.customName

						// Lecterns
						if (state.block is LecternBlock && state.getValue(LecternBlock.HAS_BOOK)) {
							foundType = searchItemStack((be as LecternBlockEntity).book, toFind, toFindTag, true)
							// Inventories (Chests etc)
						} else if (be is Container) {
							foundType = invContains(be as Container, toFind, toFindTag, true)
							// Alternative inventories (Composters)
						} else if (state.block is WorldlyContainerHolder) {
							val inv: Container? =
								(state.block as WorldlyContainerHolder).getContainer(state, world, pos)
							if (inv != null) foundType = invContains(inv, toFind, toFindTag, true)
						}
						if (foundType !== FoundType.NOT_FOUND) {
							positions[pos.immutable()] =
								SearchResult(foundType, invName)
						}
					}
				}
			}
		}

		println("Checked $checkedBECount BlockEntities")
		return positions
	}

	fun searchItemStack(itemStack: ItemStack, toFind: Item, toFindTag: CompoundTag?, deepSearch: Boolean): FoundType {
		return if (itemStack.item === toFind && (toFindTag == null || toFindTag == itemStack.tag)) {
			FoundType.FOUND
		} else {
			FoundType.NOT_FOUND
		}
	}

	private fun invContains(
		inv: Container,
		searchingFor: Item,
		searchingForNbt: CompoundTag?,
		deepSearch: Boolean
	): FoundType {
		for (i in 0 until inv.containerSize) {
			val result = searchItemStack(inv.getItem(i), searchingFor, searchingForNbt, deepSearch)
			if (result !== FoundType.NOT_FOUND) return result
		}
		return FoundType.NOT_FOUND
	}

	fun areStacksEqual(item1: Item?, tag1: CompoundTag?, item2: Item?, tag2: CompoundTag?, matchNbt: Boolean): Boolean {
		return item1 == item2 && (!matchNbt || tag1 == tag2)
	}
}
