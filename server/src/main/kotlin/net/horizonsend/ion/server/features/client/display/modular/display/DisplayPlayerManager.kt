package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.getAddEntityPacket
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.entity.Relative
import org.bukkit.Bukkit.getPlayer
import java.util.UUID

class DisplayPlayerManager(val entity: net.minecraft.world.entity.Display) {
	private var shownPlayers = mutableSetOf<UUID>()

	fun sendTeleport() {
		sendPacket(shownPlayers, ClientboundTeleportEntityPacket.teleport(entity.id, PositionMoveRotation.of(entity), setOf<Relative>(), entity.onGround))
	}

	fun sendRemove() {
		sendPacket(shownPlayers, ClientboundRemoveEntitiesPacket(entity.id))
		shownPlayers = mutableSetOf()
	}

	fun sendPacket(players: Collection<UUID>, packet: Packet<*>) {
		players.mapNotNull(::getPlayer).forEach { bukkitPlayer -> bukkitPlayer.minecraft.connection.send(packet) }
	}

	fun runUpdates() {
		val chunk = entity.level().getChunkIfLoaded(entity.x.toInt().shr(4), entity.z.toInt().shr(4)) ?: return
		val viewers = chunk.`moonrise$getChunkAndHolder`().holder.`moonrise$getPlayers`(false).mapTo(mutableSetOf(), ServerPlayer::getUUID)

		val new = viewers.minus(shownPlayers)
		val retained = viewers.intersect(shownPlayers)

		if (new.isNotEmpty()) {
			val addEntity = getAddEntityPacket(entity)
			sendPacket(new, addEntity)
		}

		val all = retained.union(new)
		if (all.isNotEmpty()) {
			all.mapNotNull(::getPlayer).forEach { bukkitPlayer -> entity.refreshEntityData(bukkitPlayer.minecraft) }
		}

		shownPlayers = viewers
	}
}
