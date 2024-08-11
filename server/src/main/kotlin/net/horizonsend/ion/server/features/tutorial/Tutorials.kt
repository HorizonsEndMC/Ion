package net.horizonsend.ion.server.features.tutorial

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.level.chunk.LevelChunkSection
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkUnloadEvent
import java.lang.ref.WeakReference
import java.util.UUID

object Tutorials : IonServerComponent() {
	var playersInTutorials = mutableMapOf<Player, TutorialPhase>()
	private var readTimes = mutableMapOf<UUID, Long>()

	private val tutorials: Set<TutorialCompanion> = setOf(
		FlightTutorialPhase
	)

	fun isTutorialWorld(world: World) = tutorials.any { it.isTutorialWorld(world) }

	operator fun get(world: World): TutorialCompanion? = tutorials.find { it.isTutorialWorld(world) }

	override fun onEnable() {
		if (!IonServer.featureFlags.tutorials) return

		listen<PlayerJoinEvent> { event ->
			val player = event.player

			player.resetTitle()
			playersInTutorials.remove(player) // who knows...

			get(player.world)?.start(player)
		}

		listen<PlayerQuitEvent> { event ->
			tutorials.forEach { it.stop(event.player) }
		}

		listen<BlockBreakEvent> { event ->
			if (isTutorialWorld(event.block.world)) event.isCancelled = true
		}

		// Disable all damage in the world
		listen<EntityDamageEvent> { event ->
			if (isTutorialWorld(event.entity.world)) {
				event.isCancelled = true
			}
		}

		// erase chunks in the world
		listen<ChunkUnloadEvent> { event ->
			if (!isTutorialWorld(event.world)) {
				return@listen
			}

			val chunk = event.chunk
			val chunkReference = WeakReference(chunk)

			val worldShipData = DeactivatedPlayerStarships.getInChunk(chunk)
			if (worldShipData.any()) {
				log.warn("Deleting " + worldShipData.size + " starship computers in tutorial world")
				DeactivatedPlayerStarships.destroyManyAsync(worldShipData) {
					clearChunk(chunkReference)
				}
				return@listen
			}

			clearChunk(chunkReference)
		}

		tutorials.forEach { tutorial ->
			tutorial.onEnable()

			tutorial.entries.forEach {
				it.setupHandlers()
			}
		}
	}

	fun clearChunk(chunkReference: WeakReference<Chunk>) {
		val chunk = chunkReference.get() ?: return
		val nmsChunk = chunk.minecraft
		val sections = nmsChunk.sections

		for (it in nmsChunk.blockEntities.keys.toList()) {
			nmsChunk.level.removeBlockEntity(it)
		}

		for (oldSection in sections.withIndex()) {
			val (y, _) = oldSection

// 			 sections[y] = LevelChunkSection(y, nmsChunk.biomeRegistry, nmsChunk.pos, nmsChunk.level)
 		}
	}
}
