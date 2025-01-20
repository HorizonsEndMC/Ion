package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.entity.Relative
import org.bukkit.Bukkit.getPlayer
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftBlockDisplay
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID

class BlockDisplayWrapper(
	val world: World,
	initPosition: Vector,
	initHeading: Vector,
	initTransformation: Vector,
	val blockData: BlockData
) {
	private var shownPlayers = mutableSetOf<UUID>()
	private val scale: Float = 1.0f

	var position: Vector = initPosition
		set(value) {
			field = value

			entity.teleportTo(value.x, value.y, value.z)

			val packet = ClientboundTeleportEntityPacket.teleport(entity.id, PositionMoveRotation.of(entity), setOf<Relative>(), entity.onGround)
			shownPlayers.map(::getPlayer).forEach { it?.minecraft?.connection?.send(packet) }
		}

	var heading: Vector = initHeading
		set(value) {
			field = value

			updateTransformation(entity)
		}

	var offset: Vector = initTransformation
		set(value) {
			field = value
			updateTransformation(entity)
		}

	private var entity: Display.BlockDisplay = createEntity().getNMSData(
		position.x,
		position.y,
		position.z
	)

	private fun createEntity(): CraftBlockDisplay = CraftBlockDisplay(
		IonServer.server as CraftServer,
		Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world.minecraft)
	).apply {
		teleportDuration = 1
		interpolationDuration = 1
		viewRange = 1000f
		brightness = org.bukkit.entity.Display.Brightness(15, 15)

		transformation = Transformation(
			offset.toVector3f(),
			ClientDisplayEntities.rotateToFaceVector(heading.toVector3f()),
			Vector3f(scale),
			Quaternionf()
		)

		block = this@BlockDisplayWrapper.blockData
	}

	fun updateTransformation(entity: Display.BlockDisplay) {
		entity.setTransformation(com.mojang.math.Transformation(
			offset.toVector3f(),
			ClientDisplayEntities.rotateToFaceVector(heading.toVector3f()),
			Vector3f(scale),
			Quaternionf()
		))

		update()
	}

	fun remove() {
		for (shownPlayer in shownPlayers) getPlayer(shownPlayer)?.minecraft?.connection?.send(
			ClientboundRemoveEntitiesPacket(entity.id)
		)

		shownPlayers.clear()
	}

	fun update() {
		val chunk = entity.level().world.getChunkAtIfLoaded(entity.x.toInt().shr(4), entity.z.toInt().shr(4)) ?: return
		val playerChunk = chunk.minecraft.`moonrise$getChunkAndHolder`().holder ?: return

		val chunkViewers = playerChunk.`moonrise$getPlayers`(false).toSet()
		val newViewers = chunkViewers.filterNot { shownPlayers.contains(it.uuid) }
		val existingViewers = chunkViewers.filter { shownPlayers.contains(it.uuid) }
		val lostViewers = shownPlayers.minus(newViewers.mapTo(mutableSetOf()) { it.uuid }).minus(existingViewers.mapTo(mutableSetOf()) { it.uuid })

		for (player in newViewers) {
			broadcast(player)
		}

		for (player in existingViewers) {
			update(player)
		}

		for (player in lostViewers) {
			getPlayer(player)?.minecraft?.connection?.send(ClientboundRemoveEntitiesPacket(entity.id))
		}

		shownPlayers = chunkViewers.mapTo(mutableSetOf()) { it.uuid }
	}

	private fun update(player: ServerPlayer) {
		entity.refreshEntityData(player)
	}

	private fun broadcast(player: ServerPlayer) {
		ClientDisplayEntities.sendEntityPacket(player.bukkitEntity, entity)
		shownPlayers.add(player.uuid)
	}

	fun getEntity() = entity
}
