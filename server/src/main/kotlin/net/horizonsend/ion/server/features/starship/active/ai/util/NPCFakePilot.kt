package net.horizonsend.ion.server.features.starship.active.ai.util

import net.citizensnpcs.api.event.NPCDamageByEntityEvent
import net.citizensnpcs.api.event.NPCDeathEvent
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships.isActive
import net.horizonsend.ion.server.features.starship.damager.damager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.createNamedMemoryRegistry
import net.horizonsend.ion.server.miscellaneous.utils.isCitizensLoaded
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
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
		Tasks.syncRepeat(10L, 10L, ::tickPilots)
	}

	fun tickPilots() {
		for ((ship, npc) in activeFakePilots) {
			if (!npc.isSpawned) npc.spawn(getLocation(ship.data))

			val entity = npc.entity ?: continue

			if (entity.isDead) {
				StarshipDestruction.destroy(ship)
			}

			entity.isGlowing = true
			entity.location.pitch = ship.controller.pitch
			entity.location.yaw = ship.controller.yaw
			entity.isInvulnerable = false
		}
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

	fun getLocation(data: StarshipData): Location {
		val computerLoc = Vec3i(data.blockKey).toLocation(data.bukkitWorld())

		return computerLoc.add(0.5, 1.0, 0.5)
	}

	/** If location is null, one will be generated from the ship computer location */
	fun add(starship: ActiveControlledStarship, location: Location?): NPC? {
		val spawnLoc = location ?: getLocation(starship.data)
		if (!isCitizensLoaded) return null

		val npc = pilotRegistry.createNPC(
			EntityType.PLAYER,
			UUID.randomUUID(),
			index,
			legacyAmpersand().serialize(starship.controller.pilotName),
		)

		npc.isProtected = false
//		npc.getOrAddTrait(SkinTrait::class.java)
//		(npc.traits.first() as SkinTrait).setSkinPersistent()
		npc.spawn(spawnLoc)


		index++
		activeFakePilots[starship] = npc

		return npc
	}

	fun remove(starship: ActiveStarship) {
		if (!isCitizensLoaded) return
		activeFakePilots.remove(starship)?.destroy()
	}

	fun isFakePilot(npc: NPC) = pilotRegistry.contains(npc)

	/** If the fake pilot of the starship is destroyed */
	@EventHandler
	fun onPilotDestroyed(event: NPCDeathEvent) {
		println("event: $event")
		val npc = event.npc
		if (!isFakePilot(npc)) return
		println("2")

		val ship = activeFakePilots.filterValues { it == npc }.keys.firstOrNull() ?: return
		println("3")
		Tasks.sync { PilotedStarships.unpilot(ship)
			println("4")}
	}

	@EventHandler
	fun onPilotDamaged(event: NPCDamageByEntityEvent) {
		println("event: $event")
		val damager = event.damager
		if (damager !is Player) return
		println("2")

		val npc = event.npc
		if (!isFakePilot(npc)) return
		println("3")

		val ship = activeFakePilots.filterValues { it == npc }.keys.firstOrNull() ?: return
		println("4")
		Tasks.sync { ship.addToDamagers(damager.damager())
			println("5")}
	}
}
