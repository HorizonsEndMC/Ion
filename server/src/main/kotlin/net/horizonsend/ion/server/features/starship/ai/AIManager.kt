package net.horizonsend.ion.server.features.starship.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import org.bukkit.event.EventHandler

object AIManager : IonServerComponent() {
	val serviceExecutor = AIServiceExecutor()

	override fun onEnable() {
		serviceExecutor.initialize()
	}

	override fun onDisable() {
		serviceExecutor.shutDown()
	}

	// Remove this until theres more interesting mechanics
//	@EventHandler
//	fun onBlockBreak(event: BlockBreakEvent) {
//		if (event.block.type != Material.JUKEBOX) return
//
//		val aiShips = ActiveStarships.getInWorld(event.block.world).filter { it.controller is AIController }
//
//		val x = event.block.x
//		val y = event.block.y
//		val z = event.block.z
//
//		val ship = aiShips.firstOrNull { it.blocks.contains(blockKey(x, y, z)) } as? ActiveControlledStarship ?: return
//
//		event.player.alert("Starship computer destroyed. AI Ship powering down...")
//
//		DeactivatedPlayerStarships.deactivateAsync(ship) {
//			DeactivatedPlayerStarships.destroyAsync(ship.data) {}
//		}
//	}

	@EventHandler
	fun onAIUnpilot(event: StarshipUnpilotEvent) {
		val starship = event.starship

		if (starship.controller !is AIController && !starship.isExploding) return

		StarshipDestruction.vanish(starship)
	}
}
