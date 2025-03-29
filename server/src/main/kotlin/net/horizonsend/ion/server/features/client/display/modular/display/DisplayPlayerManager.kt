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

class DisplayPlayerManager(val entity: net.minecraft.world.entity.Display, private val updateInvervalMS: Long = 1000L, private val playerFilter: (ServerPlayer) -> Boolean = { true }) {
	private var lastUpdate = 0L
	private var shownPlayers = mutableSetOf<UUID>()

	fun sendTeleport() {
		sendPacket(shownPlayers, ClientboundTeleportEntityPacket.teleport(entity.id, PositionMoveRotation.of(entity), setOf<Relative>(), entity.onGround))
	}

	fun sendAllRemove() {
		sendPacket(shownPlayers, ClientboundRemoveEntitiesPacket(entity.id))
		shownPlayers = mutableSetOf()
	}

	fun sendRemove(players: Collection<UUID>) {
		sendPacket(players, ClientboundRemoveEntitiesPacket(entity.id))
	}

	fun sendPacket(players: Collection<UUID>, packet: Packet<*>) {
		players.mapNotNull(::getPlayer).forEach { bukkitPlayer -> bukkitPlayer.minecraft.connection.send(packet) }
	}

	fun sendAddEntity(players: Set<UUID>) {
		val addEntity = getAddEntityPacket(entity)
		sendPacket(players, addEntity)
	}

	fun runUpdates() {
		val chunk = entity.level().getChunkIfLoaded(entity.x.toInt().shr(4), entity.z.toInt().shr(4)) ?: return
		val viewerPlayers = chunk.`moonrise$getChunkAndHolder`().holder.`moonrise$getPlayers`(false).filter(playerFilter)

		val viewerIDs = viewerPlayers.mapTo(mutableSetOf(), ServerPlayer::getUUID)

		val new = viewerIDs.minus(shownPlayers)
		val retained = viewerIDs.intersect(shownPlayers)
		val lost = shownPlayers.minus(viewerIDs)

		if (new.isNotEmpty()) sendAddEntity(new)
		if (lost.isNotEmpty()) sendRemove(lost)

		val all = retained.union(new)
		if (all.isNotEmpty() && System.currentTimeMillis() - lastUpdate > updateInvervalMS) {
			lastUpdate = System.currentTimeMillis()
			all.mapNotNull(::getPlayer).forEach { bukkitPlayer -> entity.refreshEntityData(bukkitPlayer.minecraft) }
		}

		shownPlayers = viewerIDs
	}
}
