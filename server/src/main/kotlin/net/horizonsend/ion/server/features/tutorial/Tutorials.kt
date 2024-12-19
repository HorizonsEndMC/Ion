package net.horizonsend.ion.server.features.tutorial

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.tutorial.tutorials.FlightTutorial
import net.horizonsend.ion.server.features.tutorial.tutorials.Tutorial
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.Chunk
import org.bukkit.event.player.PlayerQuitEvent
import java.lang.ref.WeakReference

object Tutorials : IonServerComponent() {
	private val tutorials: Set<Tutorial> = setOf(
		FlightTutorial,
		//TODO IntroTutorial
		// CombatTutorial
	)

	override fun onEnable() {
		if (!ConfigurationFiles.featureFlags().tutorials) return

		listen<PlayerQuitEvent> { event ->
			tutorials.forEach { it.endTutorial(event.player) }
		}

		tutorials.forEach { tutorial ->
			tutorial.setup()

			tutorial.allPhases.forEach { it.setupHandlers() }
		}
	}

	fun allTutorials() = tutorials.toList()

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
