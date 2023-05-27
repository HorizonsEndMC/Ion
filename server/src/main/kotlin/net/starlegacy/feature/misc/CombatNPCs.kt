package net.starlegacy.feature.misc

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCDeathEvent
import net.citizensnpcs.api.npc.NPC
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.get
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.slPlayerId
import net.starlegacy.listen
import net.starlegacy.util.Notify
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
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.XMLTokener.entity
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object CombatNPCs : SLComponent() {
	private const val remainTimeMinutes = 4L

	/** Map of NPC ID to its inventory */
	private val inventories: MutableMap<Int, Array<ItemStack?>> = mutableMapOf()
	private val npcToPlayer = mutableMapOf<UUID, NPC>()

	override fun onEnable() {
		// weirdness happens when someone already logged in logs on. this is my hacky fix.
		val lastJoinMap = mutableMapOf<UUID, Long>()

		// when a player joins, remove their combat npc
		listen<PlayerJoinEvent>(EventPriority.LOWEST) { event ->
			val player = event.player
			val playerId: UUID = player.uniqueId

			lastJoinMap[playerId] = System.currentTimeMillis()
		}

		// TODO: Combat NPCs are broken anyway, and they are the only thing creating chunk tickets so they could be the cause of lag right now, look into this whenever we try and bring back Combat NPCs.
		//when a player quits, create a combat npc
		listen<PlayerQuitEvent> { event ->
			val player = event.player
			val playerId = player.uniqueId

			// if this permission is granted, do not spawn the npc
			if (player.hasPermission("starlegacy.combatnpc.bypass")) {
				return@listen
			}

			// if they joined less than a second ago, don't do it
			if (System.currentTimeMillis() - (lastJoinMap[playerId] ?: 0) < 10000) {
				return@listen
			}

			val inventoryCopy: Array<ItemStack?> = player.inventory.contents
				.map { item: ItemStack? -> item?.clone() }
				.toTypedArray()

			val npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.name, player.location)
			npc.entity.customName(text("${player.name} [NPC]"))
			npc.isProtected = false
			npc.spawn(player.location)

			inventories[npc.id] = inventoryCopy
			npcToPlayer[playerId] = npc

			Tasks.syncDelay(20L * 60L * remainTimeMinutes) {
				if (!npc.isSpawned) return@syncDelay

				inventories.remove(npc.id)
				npcToPlayer.remove(playerId)
				npc.destroy()
				CitizensAPI.getNPCRegistry().deregister(npc)
			}
		}

		listen<NPCDeathEvent>(EventPriority.LOWEST) { event ->
			val npc = event.npc
			val playerId = npcToPlayer.entries.find { it.value.id == npc.id }?.key ?: return@listen

			val entity = npc.entity
			val drops: Array<ItemStack?> = inventories.remove(npc.id) ?: arrayOf()

			val killer: Entity? = (entity.lastDamageCause as? EntityDamageByEntityEvent)?.damager

			Bukkit.getPlayer(playerId)?.let { onlinePlayer ->
				log.warn("NPC for ${onlinePlayer.name} was killed while they were online!")
				return@listen
			}

			event.drops.addAll(drops)

			inventories.remove(npc.id)
			npcToPlayer.remove(playerId)

			CitizensAPI.getNPCRegistry().deregister(npc)

			transaction { PlayerData[playerId]?.wasKilled = true }
			Tasks.async {
				val name: String = SLPlayer.getName(playerId.slPlayerId) ?: "UNKNOWN"
				Notify.all("<red>Combat NPC of $name was slain by ${killer?.name}")
				Tasks.sync {
					CombatNPCKillEvent(playerId, name, killer).callEvent()
				}
			}
		}

		listen<PlayerJoinEvent> { event ->
			npcToPlayer[event.player.uniqueId]?.let {
				println("destroying")
				it.destroy()

				CitizensAPI.getNPCRegistry().deregister(it)
			}

			if (transaction { PlayerData[event.player].wasKilled }) {
				event.player.inventory.clear()
				event.player.health = 0.0
				event.player.userError("Your NPC was killed while you were offline.")
			}
		}

		listen<PlayerDeathEvent>(priority = EventPriority.LOWEST) { event ->
			transaction {
				val data = PlayerData[event.player]
				if (data.wasKilled) {
					event.drops.clear()
					event.deathMessage(null)

					data.wasKilled = false
				}
			}
		}
	}
}

class CombatNPCKillEvent(val id: UUID, val name: String, val killer: Entity?) : Event() {
	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList = handlerList
}
