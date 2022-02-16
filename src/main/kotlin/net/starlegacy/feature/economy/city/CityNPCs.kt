package net.starlegacy.feature.economy.city

import java.util.UUID
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.npc.MemoryNPCDataStore
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.citizensnpcs.trait.LookClose
import net.citizensnpcs.trait.SkinTrait
import net.starlegacy.SLComponent
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.economy.CityNPC
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.feature.economy.bazaar.Bazaars
import net.starlegacy.feature.economy.bazaar.Merchants
import net.starlegacy.feature.economy.cargotrade.ShipmentManager
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.Skins
import net.starlegacy.util.Tasks
import net.starlegacy.util.loadChunkAsync
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

/**
 * Manages NPCs for cities, handles the synchronization of them with the worlds
 */
object CityNPCs : SLComponent() {
	private val isCitizensLoaded get() = plugin.server.pluginManager.isPluginEnabled("Citizens")

	private lateinit var citizensRegistry: NPCRegistry
	private val npcTypeMap = mutableMapOf<UUID, CityNPC.Type>()

	// keep track of territories with bazaar npcs
	var BAZAAR_CITY_TERRITORIES: Set<Oid<Territory>> = setOf()

	override fun onEnable() {
		if (!isCitizensLoaded) {
			log.warn("Citizens not loaded! No NPCs!")
			return
		} else {
			log.info("Citizens hooked!")
		}

		synchronizeNPCs()
	}

	override fun onDisable() {
		// Citizens doesn't clear entities properly on reload.
		// Our plugins are reload safe, so we must do it manually.
		clearCitizensNpcs()
	}


	fun getCityNpcType(npc: NPC): CityNPC.Type? = npcTypeMap[npc.uniqueId]

	private data class NpcInfo(
		val id: Oid<CityNPC>,
		val name: String,
		val location: Location,
		val type: CityNPC.Type,
		val skin: Skins.SkinData
	)

	/**
	 * This methods removes all current tracked Citizens NPCs.
	 * Then, it creates new Citizens NPCs for every NPC in the database.
	 */
	fun synchronizeNPCsAsync(callback: (() -> Unit) = { }) {
		Tasks.async {
			synchronizeNPCs(callback)
		}
	}

	private fun synchronizeNPCs(callback: () -> Unit = { }) {
		if (!isCitizensLoaded) {
			return
		}

		val newBazaarTerritories = mutableSetOf<Oid<Territory>>()

		val npcInfo: List<NpcInfo> = CityNPC.all().toList().mapNotNull { npc ->
			val id = npc._id

			val type: CityNPC.Type = npc.type

			val name: String = type.displayName

			val territoryId: Oid<Territory> = npc.territory
			if (type == CityNPC.Type.BAZAAR) {
				newBazaarTerritories.add(territoryId)
			}

			val territory: RegionTerritory = Regions[territoryId]

			val world: World = territory.bukkitWorld ?: return@mapNotNull null

			val x: Double = npc.x
			val y: Double = npc.y
			val z: Double = npc.z
			val location = Location(world, x, y, z)

			val skin = Skins.SkinData.fromBytes(npc.skinData)

			return@mapNotNull NpcInfo(id, name, location, type, skin)
		}

		BAZAAR_CITY_TERRITORIES = newBazaarTerritories

		Tasks.sync {
			npcTypeMap.clear()
			clearCitizensNpcs()
			citizensRegistry = CitizensAPI.createNamedNPCRegistry("trade-city-npcs", MemoryNPCDataStore())

			val spawned = mutableSetOf<Oid<CityNPC>>()

			// create new NPCs and update the ID list
			npcInfo.forEach { info: NpcInfo ->
				val location = info.location

				val npc = citizensRegistry.createNPC(EntityType.PLAYER, "${SLTextStyle.GOLD}${info.name}")
				npcTypeMap[npc.uniqueId] = info.type

				loadChunkAsync(location.world, location) {
					if (!spawned.add(info.id)) {
						log.warn("Spawn task called more than once for city NPC $info")
						return@loadChunkAsync
					}

					spawnNPC(location, npc, info)
				}

				log.debug("Created NPC ${npc.uniqueId} (${npc.name})")
			}

			callback()
		}
	}

	private fun spawnNPC(location: Location, npc: NPC, info: NpcInfo) {
		check(location.isChunkLoaded)

		npc.getTrait(SkinTrait::class.java).apply {
			setSkinPersistent(info.name, info.skin.signature, info.skin.value)
		}

		npc.getTrait(LookClose::class.java).apply {
			lookClose(true)
			setRealisticLooking(true)
		}

		npc.isProtected = true

		npc.spawn(location)
	}

	/**
	 * Open shipment dialog when players click importer NPCs.
	 */
	@EventHandler
	fun onClickNPC(event: NPCRightClickEvent) {
		val player: Player = event.clicker
		val npc: NPC = event.npc
		log.info("clicked ${player.name} clicked npc ${npc.name}")

		val type: CityNPC.Type = getCityNpcType(npc) ?: return
		log.info("type $type")

		val territory = Regions.findFirstOf<RegionTerritory>(npc.storedLocation) ?: return
		log.info("Territory: ${territory.name}")

		val cityInfo: TradeCityData = TradeCities.getIfCity(territory) ?: return
		log.info("Trade city: $cityInfo")

		when (type) {
			CityNPC.Type.EXPORTER -> ShipmentManager.openShipmentSelectMenu(player, cityInfo)
			CityNPC.Type.IMPORTER -> ShipmentManager.onImport(player, cityInfo)
			CityNPC.Type.BAZAAR -> Bazaars.onClickBazaarNPC(player, cityInfo)
			CityNPC.Type.MERCHANT -> Merchants.onClickMerchantNPC(player, cityInfo)
		}
	}

	private fun clearCitizensNpcs() {
		if (CityNPCs::citizensRegistry.isInitialized) {
			citizensRegistry.toList().forEach { it.destroy() }
			citizensRegistry.deregisterAll()
			CitizensAPI.removeNamedNPCRegistry("trade-city-npcs")
		}
	}
}
