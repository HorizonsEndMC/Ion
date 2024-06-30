package net.horizonsend.ion.server.features.client.display.container

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftTextDisplay
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.TextDisplay.TextAlignment.CENTER
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID

class TextDisplayHandler(
	val world: World,
	var x: Double,
	var y: Double,
	var z: Double,
	var facing: BlockFace
) {
	private val shownPlayers = mutableListOf<UUID>()
	private val nmsEntity: Display.TextDisplay =
		CraftTextDisplay(
			IonServer.server as CraftServer,
			Display.TextDisplay(EntityType.TEXT_DISPLAY, world.minecraft)
		).apply {
			billboard = Billboard.FIXED
			viewRange = 5.0f
			brightness = Brightness(15, 15)
			teleportDuration = 0
			backgroundColor = Color.fromARGB(0x00000000)
			alignment = CENTER

			transformation = Transformation(
				Vector3f(0f),
				ClientDisplayEntities.rotateToFaceVector2d(facing.direction.toVector3f()),
				Vector3f(1.0f),
				Quaternionf()
			)
		}.getNMSData(x, y, z)

	fun setText(text: Component) {
		nmsEntity.text = PaperAdventure.asVanilla(text)
		update()
	}

	private fun broadcast(player: ServerPlayer) {
		ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
		shownPlayers.add(player.uuid)
	}

	private fun update(player: ServerPlayer) {
		nmsEntity.entityData.refresh(player)
	}

	fun update() {
		val chunk = world.getChunkAtIfLoaded(x.toInt().shr(4), z.toInt().shr(4)) ?: return
		val playerChunk = chunk.minecraft.playerChunk ?: return

		for (player in playerChunk.getPlayers(false)) {
			if (shownPlayers.contains(player.uuid)) update(player) else broadcast(player)
		}
	}

	fun remove() {
		for (shownPlayer in shownPlayers) Bukkit.getPlayer(shownPlayer)?.minecraft?.connection?.send(
			ClientboundRemoveEntitiesPacket(nmsEntity.id)
		)
	}
}
