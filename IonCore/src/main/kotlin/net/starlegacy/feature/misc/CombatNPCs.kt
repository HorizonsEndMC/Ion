package net.starlegacy.feature.misc

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.slPlayerId
import net.starlegacy.util.Notify
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.Tasks
import net.starlegacy.util.colorize
import net.starlegacy.util.msg
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object CombatNPCs : SLComponent() {
	private const val remainTimeMinutes = 4L
	private val helmetText = "${SLTextStyle.OBFUSCATED}COMBAT NPC"

	/** Map of NPC entity ID to player ID */
	private val map: BiMap<UUID, UUID> = HashBiMap.create()

	// convenience aliases which are inlined at compile time
	private inline val entityToPlayer get() = map
	private inline val playerToEntity get() = map.inverse()

	/** Map of NPC ID to its inventory */
	private val inventories: MutableMap<UUID, Array<ItemStack?>> = mutableMapOf()

	/** map of armor stand id to
	 * hacky fix for armor stands dropping their helmets on death... */
	private val helmetMap = mutableMapOf<UUID, ItemStack>()

	override fun onEnable() {
		subscribe<PlayerJoinEvent>(EventPriority.LOWEST) { event -> KilledPlayers.onJoin(event.player) }

		// weirdness happens when someone already logged in logs on. this is my hacky fix.
		val lastJoinMap = mutableMapOf<UUID, Long>()

		// when a player joins, remove their combat npc
		subscribe<PlayerJoinEvent>(EventPriority.LOWEST) { event ->
			val player = event.player
			val playerId: UUID = player.uniqueId
			lastJoinMap[playerId] = System.currentTimeMillis()

			val npcId: UUID = playerToEntity.remove(playerId) ?: return@subscribe

			plugin.server.getEntity(npcId)?.remove()
			inventories.remove(npcId)
		}

		// when a player quits, create a combat npc
		subscribe<PlayerQuitEvent> { event ->
			val player = event.player
			val playerId = player.uniqueId
			val chunk = player.chunk
			chunk.addPluginChunkTicket(plugin)

			inventories.remove(playerId)
			// attempt to remove entity from map based on player id.
			// if one is removed, also attempt to get a currently loaded entity from the removed id.
			// if it is present, remove that entity as well.
			playerToEntity.remove(playerId)?.also { oldEntityId: UUID ->
				plugin.server.getEntity(oldEntityId)?.remove()
				inventories.remove(oldEntityId)
			}

			// if this permission is granted, do not spawn the npc
			if (player.hasPermission("starlegacy.combatnpc.bypass")) {
				return@subscribe
			}
			// if they joined less than a second ago, don't do it
			if (System.currentTimeMillis() - (lastJoinMap[playerId] ?: 0) < 1000) {
				return@subscribe
			}

			val entity: ArmorStand = spawnEntity(player)
			val entityId = entity.uniqueId

			entityToPlayer[entityId] = playerId

			val inventoryCopy: Array<ItemStack?> = player.inventory.contents!!
				.map { item: ItemStack? -> item?.clone() }
				.toTypedArray()
			inventories[entityId] = inventoryCopy

			Tasks.syncDelay(20L * 60L * remainTimeMinutes) {
				entityToPlayer.remove(entityId, playerId)
				plugin.server.getEntity(entityId)?.remove()
				inventories.remove(entityId)
				chunk.removePluginChunkTicket(plugin)
			}
		}

		subscribe<PlayerArmorStandManipulateEvent> { event ->
			if (isNPC(event.rightClicked)) {
				event.isCancelled = true
			}
		}

		// hacky fix for armor stands dropping their helmets on death...
		subscribe<EntityDamageEvent> { event ->
			val entity = event.entity
			val helmet = (entity as? ArmorStand)?.helmet ?: return@subscribe
			helmetMap[entity.uniqueId] = helmet
		}

		subscribe<EntityDeathEvent>(EventPriority.LOWEST) { event ->
			val entity: ArmorStand = event.entity as? ArmorStand ?: return@subscribe

			if (!isNPC(entity)) {
				return@subscribe
			}

			event.drops.clear()

			val entityId: UUID = entity.uniqueId
			val drops: Array<ItemStack?> = inventories.remove(entityId) ?: arrayOf()

			val playerId: UUID? = entityToPlayer.remove(entityId)

			val killer: Entity? = (entity.lastDamageCause as? EntityDamageByEntityEvent)?.damager

			if (playerId == null) {
				killer?.msg("&cThis combat NPC is expired. It should have despawned already.")
				return@subscribe
			}

			Bukkit.getPlayer(playerId)?.let { onlinePlayer ->
				log.warn("NPC for ${onlinePlayer.name} was killed while they were online!")
				return@subscribe
			}

			event.drops.addAll(drops)

			KilledPlayers.onKill(playerId, entity)


			Tasks.async {
				val name: String = SLPlayer.getName(playerId.slPlayerId) ?: "UNKNOWN"
				Notify.all("&cCombat NPC of $name was slain by ${killer?.name}".colorize())
				Tasks.sync {
					CombatNPCKillEvent(playerId, name, killer).callEvent()
				}
			}
		}

		// when a chunk loads, go through its entities. if it is an armor stand which has the mark of a
		// combat npc, but it's not in the map, remove it.
		subscribe<ChunkLoadEvent> { event ->
			for (entity in event.chunk.entities) {
				if (entity is ArmorStand && isNPC(entity) && !entityToPlayer.containsKey(entity.uniqueId)) {
					entity.remove()
				}
			}
		}

		KilledPlayers.load()
	}

	override fun onDisable() {
		KilledPlayers.save()
	}

	fun isNPC(entity: ArmorStand): Boolean {
		// used cached helmet, in case it disappears upon death
		val helmet = entity.helmet.takeIf { it.type != Material.AIR } ?: helmetMap[entity.uniqueId]
		return helmetText == helmet?.itemMeta?.displayName
	}

	private fun spawnEntity(player: Player): ArmorStand {
		val world = player.world
		val armorStand: ArmorStand = world.spawnEntity(player.location, EntityType.ARMOR_STAND) as ArmorStand

		armorStand.customName = player.name
		armorStand.isCustomNameVisible = true

		armorStand.setArms(true)
		armorStand.setGravity(false)
		armorStand.setCanMove(false)

		// copy inventory
		val inventory = player.inventory
		armorStand.equipment.helmet = inventory.helmet
		armorStand.equipment.chestplate = inventory.chestplate
		armorStand.equipment.chestplate = inventory.leggings
		armorStand.equipment.boots = inventory.boots
		armorStand.equipment.setItemInMainHand(inventory.itemInMainHand)
		armorStand.setItem(EquipmentSlot.HAND, inventory.itemInMainHand)

		// set skull
		armorStand.equipment.helmet = ItemStack(Material.PLAYER_HEAD, 1).apply {
			itemMeta = (itemMeta as SkullMeta).apply {
				setDisplayName(helmetText)
				playerProfile = player.playerProfile
			}
		}

		val playerMaxHealth: Double = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
		armorStand.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = playerMaxHealth
		armorStand.health = player.health

		armorStand.removeWhenFarAway = false

		return armorStand
	}

	private object KilledPlayers {
		private data class KilledPlayerData(val playerId: UUID, val timestamp: Long, val causeString: String)

		private val killedPlayers: MutableMap<UUID, KilledPlayerData> = Object2ObjectOpenHashMap()
		private val killedPlayersType: Type = TypeToken.getParameterized(
			/*class: */ HashMap::class.java,
			/*type parameters: */ UUID::class.java, KilledPlayerData::class.java
		).type

		private val file = File(plugin.dataFolder, "combat_npc_killed_player_data.json")

		fun load() {
			synchronized(file) {
				if (file.exists()) {
					killedPlayers.clear()
					killedPlayers.putAll(FileReader(file).use { reader ->
						Gson().fromJson<HashMap<UUID, KilledPlayerData>>(reader, killedPlayersType)
					})
				}
			}
		}

		fun saveAsync() = Tasks.async { save(killedPlayers.toMap()) }

		fun save(killedPlayers: Map<UUID, KilledPlayerData> = this.killedPlayers) {
			synchronized(file) {
				FileWriter(file).use { writer ->
					Gson().toJson(killedPlayers, killedPlayersType, writer)
				}
			}
		}

		fun onKill(playerId: UUID, entity: ArmorStand) {
			val damager: Entity? = getDamager(entity)
			val causeString: String = when {
				damager != null -> "Slain by ${damager.name}"
				else -> "Unknown"
			}
			killedPlayers[playerId] = KilledPlayerData(playerId, System.currentTimeMillis(), causeString)
			saveAsync()
		}

		fun onJoin(player: Player) {
			val data = killedPlayers.remove(player.uniqueId) ?: return
			saveAsync()

			// delay message sending to be past other welcome messages
			Tasks.syncDelay(2) {
				// this is necessary because of a weird mix of kotlin nullability,
				// and plain old clear() not clearing some slots like offhand
				@Suppress("UNCHECKED_CAST")
				player.inventory.contents =
					arrayOfNulls<ItemStack?>(player.inventory.contents!!.size) as Array<ItemStack>
				player.health = 0.0

				val zonedDateTime: ZonedDateTime = Instant.ofEpochMilli(data.timestamp).atZone(ZoneId.systemDefault())

				player msg "&c&oWhile you were offline, you were killed. " +
					"Time: &b&o${DateTimeFormatter.ISO_DATE_TIME.format(zonedDateTime)}&c&o, " +
					"Cause: &7&o${data.causeString}"
			}
		}
	}

	private fun getDamager(entity: ArmorStand): Entity? {
		return (entity.lastDamageCause as? EntityDamageByEntityEvent)?.damager
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}

class CombatNPCKillEvent(val id: UUID, val name: String, val killer: Entity?) : Event() {
	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList = handlerList
}