package net.horizonsend.ion.server.features.client.elsed

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.elsed.display.Display
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display.TextDisplay
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.UUID

class TextDisplayHandler(val world: World, val x: Double, val y: Double, val z: Double, vararg display: Display) {
	private val displays = listOf(*display)

	private var shownPlayers = mutableSetOf<UUID>()

	fun update(entity: TextDisplay) {
		val chunk = entity.level().world.getChunkAtIfLoaded(x.toInt().shr(4), z.toInt().shr(4)) ?: return
		val playerChunk = chunk.minecraft.playerChunk ?: return

		val viewers = playerChunk.getPlayers(false).toSet()
		val newPlayers = viewers.filterNot { shownPlayers.contains(it.uuid) }
		val old = viewers.filter { shownPlayers.contains(it.uuid) }

		for (player in newPlayers) {
			broadcast(player, entity)
		}

		for (player in old) {
			update(player, entity)
		}

		shownPlayers = viewers.mapTo(mutableSetOf()) { it.uuid }
	}

	private fun update(player: ServerPlayer, entity: TextDisplay) {
		entity.entityData.refresh(player)
	}

	private fun broadcast(player: ServerPlayer, entity: TextDisplay) {
		ClientDisplayEntities.sendEntityPacket(player, entity)
		shownPlayers.add(player.uuid)
	}

	fun update() {
		displays.forEach {
			update(it.entity)
		}
	}

	fun remove() {
		displays.forEach {
			for (shownPlayer in shownPlayers) Bukkit.getPlayer(shownPlayer)?.minecraft?.connection?.send(
				ClientboundRemoveEntitiesPacket(it.entity.id)
			)

			it.deRegister()
		}

		shownPlayers.clear()
	}

	fun register(): TextDisplayHandler {
		displays.forEach {
			it.setParent(this)
			it.register()
		}

		// TODO schedule updates

		return this
	}
}
