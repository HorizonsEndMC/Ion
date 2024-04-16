package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.Material
import java.util.UUID

class GlowModule(controller: AIController) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	private val entity = ClientDisplayEntities.displayBlock(world.minecraft, JUKEBOX, getCenter().toVector(), 2.5f, true)
	private val seen = LinkedHashMap<UUID, Long>()

	override fun onMove(movement: StarshipMovement) {
		val newLoc = movement.displaceLocation(getCenter().toLocation(world))

		entity.setPos(newLoc.x, newLoc.y, newLoc.z)

		for (player in newLoc.chunk.minecraft.playerChunk?.getPlayers(false).orEmpty()) {
			ClientDisplayEntities.moveDisplayEntityPacket(
				player,
				entity,
				newLoc.x,
				newLoc.y,
				newLoc.z
			)
		}

		if (movement.newWorld == null) return

		entity.setLevel(movement.newWorld.minecraft)
	}

	private var ticks = 0

	override fun tick() {
		ticks++

		if (ticks % 20 != 0) return

		updatePlayers()
	}

	private fun updatePlayers() {
		val chunk = getCenter().toLocation(world).chunk.minecraft

		val playerChunk = chunk.playerChunk ?: return

		for (player in playerChunk.getPlayers(false)) {
			if (!seen.contains(player.uuid)) {
				ClientDisplayEntities.sendEntityPacket(player, entity, 10 * 20L)
				seen[player.uuid] = System.currentTimeMillis()
			} else if (System.currentTimeMillis() - seen.getOrDefault(player.uuid, 0) >= 5000) {
				seen.remove(player.uuid)
				ClientDisplayEntities.deleteDisplayEntityPacket(player.bukkitEntity, entity)
			}

			entity.entityData.refresh(player)
		}
	}

	companion object {
		private val JUKEBOX = Material.JUKEBOX.createBlockData()
	}
}
