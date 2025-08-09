package net.horizonsend.ion.server.features.client.display.modular

import com.mojang.math.Transformation
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
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
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftItemDisplay
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.random.Random

class ItemDisplayContainer(
    val world: World,
    initScale: Float,
    initPosition: Vector,
    initHeading: Vector,
    item: ItemStack,
	val ignorePlayerSetting: Boolean = false,
	val interpolation: Int = 3
) : DisplayWrapper {
	private var shownPlayers = mutableSetOf<UUID>()

	override var position: Vector = initPosition
		set(value) {
			field = value

			entity.teleportTo(
				value.x,
				value.y,
				value.z
			)

			entity.transformationInterpolationDuration = interpolation

			shownPlayers.map(Bukkit::getPlayer).forEach {
				it?.minecraft?.connection?.send(ClientboundTeleportEntityPacket.teleport(entity.id, PositionMoveRotation.of(entity), setOf<Relative>(), entity.onGround))
			}
		}

	override var heading: Vector = initHeading
		set(value) {
			field = value

			entity.setTransformation(
                Transformation(
					offset.toVector3f(),
					ClientDisplayEntities.rotateToFaceVector(value.toVector3f()),
					scale.toVector3f(),
                    Quaternionf()
				)
            )

			entity.transformationInterpolationDuration = interpolation
		}

	override var scale: Vector = Vector(initScale, initScale, initScale)
		set(value) {
			field = value

			entity.setTransformation(
                Transformation(
					offset.toVector3f(),
					ClientDisplayEntities.rotateToFaceVector(heading.toVector3f()),
					scale.toVector3f(),
                    Quaternionf()
				)
            )

			entity.transformationInterpolationDuration = interpolation
		}

	override var offset: Vector = Vector(0.0, 0.0, 0.0)
		set(value) {
			field = value

			entity.setTransformation(
                Transformation(
                    value.toVector3f(),
					ClientDisplayEntities.rotateToFaceVector(heading.toVector3f()),
					scale.toVector3f(),
                    Quaternionf()
				)
            )

			entity.transformationInterpolationDuration = interpolation
		}

	var itemStack: ItemStack = item
		set(value) {
			field = value
			entity.itemStack = CraftItemStack.asNMSCopy(itemStack)
		}

	private val entity: Display.ItemDisplay = createEntity().getNMSData(
		position.x,
		position.y,
		position.z
	)

	private fun createEntity(): CraftItemDisplay = CraftItemDisplay(
        IonServer.server as CraftServer,
        Display.ItemDisplay(EntityType.ITEM_DISPLAY, world.minecraft)
    ).apply {
		setItemStack(this@ItemDisplayContainer.itemStack)
		billboard = org.bukkit.entity.Display.Billboard.FIXED
		brightness = org.bukkit.entity.Display.Brightness(15, 15)
		teleportDuration = 0
		viewRange = 1000f

		transformation = org.bukkit.util.Transformation(
            Vector3f(0f),
            ClientDisplayEntities.rotateToFaceVector2d(heading.toVector3f()),
			scale.toVector3f(),
            Quaternionf()
        )
	}

	override fun remove() {
		for (shownPlayer in shownPlayers) Bukkit.getPlayer(shownPlayer)?.minecraft?.connection?.send(
            ClientboundRemoveEntitiesPacket(entity.id)
		)

		shownPlayers.clear()
	}

	override fun update() {
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
			Bukkit.getPlayer(player)?.minecraft?.connection?.send(ClientboundRemoveEntitiesPacket(entity.id))
		}

		shownPlayers = chunkViewers.mapTo(mutableSetOf()) { it.uuid }
	}

	private fun update(player: ServerPlayer) {
		entity.refreshEntityData(player)
	}

	private fun broadcast(player: ServerPlayer) {
		val playerSetting = ClientDisplayEntities.Visibility.entries[player.bukkitEntity.getSetting(PlayerSettings::displayEntityVisibility)]

		val display = if (ignorePlayerSetting) true else when (playerSetting) {
			ClientDisplayEntities.Visibility.ON -> true
			ClientDisplayEntities.Visibility.REDUCED -> Random.nextBoolean()
			ClientDisplayEntities.Visibility.OFF -> false
		}

		if (display) ClientDisplayEntities.sendEntityPacket(player.bukkitEntity, entity)
		shownPlayers.add(player.uuid)
	}

	override fun getEntity() = entity
}
