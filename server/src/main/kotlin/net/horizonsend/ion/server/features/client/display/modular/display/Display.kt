package net.horizonsend.ion.server.features.client.display.modular.display

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.moveDisplayEntityPacket
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display.TextDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.Axis.Y
import org.bukkit.Bukkit.getPlayer
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftTextDisplay
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID

abstract class Display(
	private val offsetRight: Double,
	private val offsetUp: Double,
	private val offsetForward: Double,
	val scale: Float
) {
	private var shownPlayers = mutableSetOf<UUID>()

	protected lateinit var handler: TextDisplayHandler
	lateinit var entity: TextDisplay; private set

	open fun createEntity(parent: TextDisplayHandler): CraftTextDisplay =  CraftTextDisplay(
		IonServer.server as CraftServer,
		TextDisplay(EntityType.TEXT_DISPLAY, parent.world.minecraft)
	).apply {
		billboard = org.bukkit.entity.Display.Billboard.FIXED
		viewRange = 5.0f
		brightness = org.bukkit.entity.Display.Brightness(15, 15)
		teleportDuration = 0
		backgroundColor = Color.fromARGB(0x00000000)
		alignment = org.bukkit.entity.TextDisplay.TextAlignment.CENTER

		transformation = Transformation(
			Vector3f(0f),
			ClientDisplayEntities.rotateToFaceVector2d(parent.facing.direction.toVector3f()),
			Vector3f(scale),
			Quaternionf()
		)
	}

	private var isInitializing = false

	val mutex = Any()

	fun getLocation(parent: TextDisplayHandler): Location {
		val rightFace = if (parent.facing.axis == Y) BlockFace.NORTH else parent.facing.rightFace

		val offsetX = (rightFace.modX * offsetRight) + (parent.facing.modX * offsetForward)
		val offsetY = offsetUp
		val offsetZ = (rightFace.modZ * offsetRight) + (parent.facing.modZ * offsetForward)

		val parentLoc = parent.getLocation()

		return Location(
			parent.world,
			parentLoc.x + offsetX,
			parentLoc.y + offsetY,
			parentLoc.z + offsetZ
		)
	}

	fun initialize(parent: TextDisplayHandler) = synchronized(mutex) {
		if (isInitializing || this::handler.isInitialized) {
			return@synchronized
		}

		isInitializing = true

		this.handler = parent

		val location = getLocation(parent)

		runCatching {
			entity = createEntity(parent).getNMSData(
				location.x,
				location.y,
				location.z
			)
		}
	}

	fun resetPosition(parent: TextDisplayHandler) {
		val location = getLocation(parent)

		entity.setTransformation(com.mojang.math.Transformation(
			Vector3f(0f),
			ClientDisplayEntities.rotateToFaceVector2d(parent.facing.direction.toVector3f()),
			Vector3f(scale),
			Quaternionf()
		))

		shownPlayers.map(::getPlayer).forEach { player ->
			if (player == null) return@forEach
			moveDisplayEntityPacket(player.minecraft, entity, location.x, location.y, location.z)
		}
	}

	/** Registers this display handler */
	abstract fun register()

	/** Registers this display handler */
	abstract fun deRegister()

	abstract fun getText(): Component

	private fun setText(text: Component) {
		entity.text = PaperAdventure.asVanilla(text)
	}

	open fun remove() {
		for (shownPlayer in shownPlayers) getPlayer(shownPlayer)?.minecraft?.connection?.send(
			ClientboundRemoveEntitiesPacket(entity.id)
		)

		shownPlayers.clear()
	}

	open fun display() {
		if (!::handler.isInitialized) return

		update()
	}

	private val distSquared = (50.0 * 50.0)

	open fun update() {
		setText(getText())

		val chunk = entity.level().world.getChunkAtIfLoaded(entity.x.toInt().shr(4), entity.z.toInt().shr(4)) ?: return
		val playerChunk = chunk.minecraft.`moonrise$getChunkAndHolder`().holder.playerProvider ?: return
		//TODO fix this its bad
		val viewers = playerChunk.getPlayers(chunk.minecraft.pos, false).toSet()
		val newPlayers = viewers.filterNot { shownPlayers.contains(it.uuid) }
		val old = viewers.filter { shownPlayers.contains(it.uuid) }

		for (player in newPlayers) {
			broadcast(player)
		}

		for (player in old) {
			update(player)
		}

		shownPlayers = viewers.mapTo(mutableSetOf()) { it.uuid }
	}

	private fun update(player: ServerPlayer) {
		entity.refreshEntityData(player)
	}

	private fun broadcast(player: ServerPlayer) {
		ClientDisplayEntities.sendEntityPacket(player, entity)
		shownPlayers.add(player.uuid)
	}
}
