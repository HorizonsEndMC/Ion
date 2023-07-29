package net.horizonsend.ion.server.features.economy.city

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.npc.MemoryNPCDataStore
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.citizensnpcs.trait.LookClose
import net.citizensnpcs.trait.SkinTrait
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.CityNPC
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Merchants
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentManager
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.loadChunkAsync
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.UUID

/**
 * Manages NPCs for cities, handles the synchronization of them with the worlds
 */
object CityNPCs : IonServerComponent(true) {
	private val isCitizensLoaded get() = IonServer.server.pluginManager.isPluginEnabled("Citizens")

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
			npcInfo.forEachIndexed { index, info: NpcInfo ->
				val location = info.location

				val npc = citizensRegistry.createNPC(
					EntityType.PLAYER,
					UUID.randomUUID(),
					1000 + index,
					"${SLTextStyle.GOLD}${info.name}"
				)
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
	@EventHandler(priority = EventPriority.LOWEST)
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

	fun Player.isCityNpc() = citizensRegistry.isNPC(this)

	fun Player.isNpc() = CitizensAPI.getNPCRegistries().any { it.isNPC(this) }
}
