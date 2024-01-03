package net.horizonsend.ion.server.features.economy.collectors

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.citizensnpcs.trait.LookClose
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.EcoStation
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.colorize
import net.horizonsend.ion.server.miscellaneous.utils.createNamedMemoryRegistry
import net.horizonsend.ion.server.miscellaneous.utils.isCitizensLoaded
import net.horizonsend.ion.server.miscellaneous.utils.loadChunkAsync
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

object Collectors : IonServerComponent(true) {
	private lateinit var citizensRegistry: NPCRegistry

	private const val npcRegistryName = "trade-collectors"

	private val npcStationCache = ConcurrentHashMap<UUID, Oid<EcoStation>>()

	override fun onEnable() {
		if (!isCitizensLoaded) {
			log.warn("Citizens not loaded! No NPCs!")
			return
		} else {
			log.info("Citizens hooked!")
		}

		synchronizeNPCsAsync()
	}

	override fun onDisable() {
		clearCitizenNPCs()
	}

	/** If the NPC is a cached collector, returns its eco station */
	fun getCollectorStation(npc: NPC): Oid<EcoStation>? = npcStationCache[npc.uniqueId]

	fun synchronizeNPCsAsync(callback: (() -> Unit) = { }) = Tasks.sync {
		if (!isCitizensLoaded) {
			return@sync
		}

		clearCitizenNPCs()
		npcStationCache.clear()

		citizensRegistry = createNamedMemoryRegistry(npcRegistryName)

		for (ecoStation in EcoStation.all()) {
			val world = Bukkit.getWorld(ecoStation.world) ?: continue

			for ((x, y, z) in ecoStation.collectors) {
				val name: String = "&b${ecoStation.name} Collector".colorize()
				val npc: NPC = citizensRegistry.createNPC(EntityType.VILLAGER, name)

				npcStationCache[npc.uniqueId] = ecoStation._id

				// center of the block
				val location = Location(world, x + 0.5, y.toDouble(), z + 0.5)

				// spawn the entity after the chunk is loaded
				loadChunkAsync(world, location) {
					npc.spawn(location)

					npc.isProtected = true

					npc.getTrait(LookClose::class.java).apply {
						lookClose(true)
						setRealisticLooking(true)
					}
				}

				log.debug("Created NPC ${npc.uniqueId}")
			}
		}

		callback() // run callback synchronously when complete
	}

	private fun clearCitizenNPCs() {
		if (Collectors::citizensRegistry.isInitialized) {
			citizensRegistry.toList().forEach(NPC::destroy)
			citizensRegistry.deregisterAll()
			CitizensAPI.removeNamedNPCRegistry(npcRegistryName)
		}
	}
}
