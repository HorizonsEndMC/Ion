package net.horizonsend.ion.server.features.starship.active.ai.util

import net.citizensnpcs.api.event.NPCDamageByEntityEvent
import net.citizensnpcs.api.event.NPCDeathEvent
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.citizensnpcs.trait.SkinTrait
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships.isPiloted
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships.isActive
import net.horizonsend.ion.server.features.starship.damager.damager
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.createNamedMemoryRegistry
import net.horizonsend.ion.server.miscellaneous.utils.isCitizensLoaded
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import org.bukkit.Location
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
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
		return
		for ((starship, npc) in activeFakePilots) {
			if (!isActive(starship)) continue
			if (!isPiloted(starship)) continue

			if (!npc.isSpawned) npc.spawn(getLocation(starship.data))

			val entity = npc.entity ?: continue

			if (entity.isDead) {
				StarshipDestruction.destroy(starship)
			}

			// Shouldn't be null since it just spawned
			val (x, y, z) = Vec3i(npc.entity.location)
			if (!starship.contains(x, y, z)) npc.entity?.teleport(getLocation(starship.data))

			entity.isGlowing = true
			entity.setGravity(false)
			entity.location.pitch = starship.controller.pitch
			entity.location.yaw = starship.controller.yaw
			entity.isInvulnerable = false
		}
	}

	override fun onDisable() {
		if (!isCitizensLoaded) return

		pilotRegistry.forEach { it.destroy() }
	}

	private fun clearInactive() {
		return
		if (!isCitizensLoaded) return

		val iterator = activeFakePilots.iterator()

		while (iterator.hasNext()) {
			val (ship, pilot) = iterator.next()

			if (isActive(ship)) continue
			if (isPiloted(ship)) continue

			// Kill the entity to drop its items
			(pilot.entity as? Damageable)?.health = 0.0
			pilot.destroy()
			iterator.remove()
		}
	}

	fun getLocation(data: StarshipData): Location {
		val computerLoc = Vec3i(data.blockKey).toLocation(data.bukkitWorld())

		return computerLoc.add(0.5, 1.0, 0.5)
	}

	/** If location is null, one will be generated from the ship computer location */
	fun add(starship: ActiveControlledStarship, location: Location?, pilotName: Component? = null): NPC? {
		return null
		val spawnLoc = location ?: getLocation(starship.data)
		if (!isCitizensLoaded) return null

		val npc = pilotRegistry.createNPC(
			EntityType.PLAYER,
			UUID.randomUUID(),
			index,
			legacyAmpersand().serialize(pilotName ?: starship.controller.pilotName),
		)

		npc.isProtected = false
		Skins["https://assets.horizonsend.net/training_droid.png"]?.let { (_, textureData, signature) ->
			val trait = npc.getOrAddTrait(SkinTrait::class.java)
			trait.setSkinPersistent("training-droid", signature, textureData)
		}

		npc.spawn(spawnLoc)

		index++
		activeFakePilots[starship] = npc

		return npc
	}

	fun remove(starship: ActiveStarship) {
		return
		if (!isCitizensLoaded) return
		activeFakePilots.remove(starship)?.destroy()
	}

	fun isFakePilot(npc: NPC) = pilotRegistry.contains(npc)
	fun isFakePilot(entity: Entity) = pilotRegistry.isNPC(entity)

	/** If the fake pilot of the starship is destroyed */
	@EventHandler
	fun onPilotDestroyed(event: NPCDeathEvent) {
		return
		val npc = event.npc
		if (!isFakePilot(npc)) return

		val ship = activeFakePilots.filterValues { it == npc }.keys.firstOrNull() ?: return
		Tasks.sync { PilotedStarships.unpilot(ship) }
	}

	@EventHandler
	fun onPilotDamaged(event: NPCDamageByEntityEvent) {
		return
		val damager = event.damager
		if (damager !is Player) return

		val npc = event.npc
		if (!isFakePilot(npc)) return

		val ship = activeFakePilots.filterValues { it == npc }.keys.firstOrNull() ?: return
		Tasks.sync { ship.addToDamagers(damager.damager()) }
	}
}
