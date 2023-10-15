package net.horizonsend.ion.server.features.starship.active.ai.util

import net.citizensnpcs.api.event.NPCDeathEvent
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships.isActive
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.createNamedMemoryRegistry
import net.horizonsend.ion.server.miscellaneous.utils.isCitizensLoaded
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID

object NPCFakePilot : IonServerComponent(true) {
	lateinit var pilotRegistry: NPCRegistry
	val activeFakePilots = mutableMapOf<ActiveControlledStarship, NPC>()
	var index = 1

	override fun onEnable() {
		if (!isCitizensLoaded) {
			log.warn("Citizens not loaded! AI Ships will not have fake pilots.")

			return
		}

		pilotRegistry = createNamedMemoryRegistry("npc-fake-pilots")

		Tasks.syncRepeat(20L, 20L, ::clearInactive)
	}

	override fun onDisable() {
		if (!isCitizensLoaded) return

		pilotRegistry.forEach { it.destroy() }
	}

	private fun clearInactive() {
		if (!isCitizensLoaded) return

		val iterator = activeFakePilots.iterator()

		while (iterator.hasNext()) {
			val (ship, pilot) = iterator.next()

			if (isActive(ship)) continue

			pilot.destroy()
			iterator.remove()
		}
	}

	fun add(starship: ActiveControlledStarship, location: Location): NPC? {
		if (!isCitizensLoaded) return null

		val npc = pilotRegistry.createNPC(
			EntityType.PLAYER,
			UUID.randomUUID(),
			index,
			legacyAmpersand().serialize(starship.controller.pilotName),
		)

		npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN)
		npc.entity.isGlowing = true

		index++
		activeFakePilots[starship] = npc

		return npc
	}

	fun remove(starship: ActiveStarship) {
		if (!isCitizensLoaded) return
		activeFakePilots.remove(starship)?.destroy()
	}

	fun isFakePilot(npc: NPC) = pilotRegistry.contains(npc)

	/** If the fake pilot of the starship is destroyed,  */
	@EventHandler
	fun onPilotDestroyed(event: NPCDeathEvent) {
		val npc = event.npc
		if (!isFakePilot(npc)) return

		val ship = activeFakePilots.filterValues { it == npc }.keys.firstOrNull() ?: return
		Tasks.sync { PilotedStarships.unpilot(ship) }
	}
}
