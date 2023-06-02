package net.horizonsend.ion.server.miscellaneous

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCDeathEvent
import net.citizensnpcs.api.npc.NPC
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.slPlayerId
import net.starlegacy.listen
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.CompletableFuture

object CombatNPCs : SLComponent() {
	private const val remainTimeMinutes = 4L

	/** Map of NPC ID to its inventory */
	private val inventories: MutableMap<Int, Array<ItemStack?>> = mutableMapOf()
	val npcToPlayer = mutableMapOf<UUID, NPC>()

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
			if (IonServer.configuration.serverName == "Creative") return@listen

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

			npc.entity.persistentDataContainer.set(
				NamespacedKeys.COMBAT_NPC,
				PersistentDataType.LONG,
				System.currentTimeMillis()
			)

			npc.entity.chunk.addPluginChunkTicket(IonServer)

			Tasks.syncDelay(20L * 60L * remainTimeMinutes) {
				if (!npc.isSpawned) return@syncDelay

				inventories.remove(npc.id)
				npcToPlayer.remove(playerId)

				destroyNPC(npc)
				npc.entity.chunk.removePluginChunkTicket(IonServer)
			}
		}

		listen<NPCDeathEvent>(EventPriority.LOWEST) { event ->
			val npc = event.npc
			val playerId = npcToPlayer.entries.find { it.value.id == npc.id }?.key ?: return@listen

			val entity = npc.entity
			entity.chunk.removePluginChunkTicket(IonServer)

			val killer: Entity? = (entity.lastDamageCause as? EntityDamageByEntityEvent)?.damager


			if (entity.persistentDataContainer.has(NamespacedKeys.COMBAT_NPC)) {
				val time = entity.persistentDataContainer.get(NamespacedKeys.COMBAT_NPC, PersistentDataType.LONG)!!

				if ((System.currentTimeMillis() - time) >= 1000L * 60 * remainTimeMinutes) {
					killer?.userError("Combat NPC expired!")

					inventories.remove(npc.id)
					npcToPlayer.remove(playerId)

					destroyNPC(npc)
					return@listen
				}
			}

			val drops: Array<ItemStack?> = inventories.remove(npc.id) ?: arrayOf()

			Bukkit.getPlayer(playerId)?.let { onlinePlayer ->
				log.warn("NPC for ${onlinePlayer.name} was killed while they were online!")
				return@listen
			}

			event.drops.addAll(drops)

			inventories.remove(npc.id)
			npcToPlayer.remove(playerId)

			destroyNPC(npc)

			transaction { PlayerData[playerId]?.wasKilled = true }
			Tasks.async {
				val name: String = SLPlayer.getName(playerId.slPlayerId) ?: "UNKNOWN"
				Notify.all(MiniMessage.miniMessage().deserialize("<red>Combat NPC of $name was slain by ${killer?.name}"))
				Tasks.sync {
					CombatNPCKillEvent(playerId, name, killer).callEvent()
				}
			}
		}

		listen<PlayerJoinEvent> { event ->
			npcToPlayer[event.player.uniqueId]?.let {
				println("destroying")

				it.entity.chunk.removePluginChunkTicket(IonServer)
				destroyNPC(it)
			}


			if (transaction { PlayerData[event.player].wasKilled }) {
				event.player.inventory.clear()
				event.player.health = 0.0
				event.player.userError("Your NPC was killed while you were offline.")
			}
		}

		listen<PlayerDeathEvent>(priority = EventPriority.LOWEST) { event ->
			transaction {
				val data = PlayerData[event.player.uniqueId]
				if (data?.wasKilled == true) {
					event.drops.clear()
					event.deathMessage(null)

					data.wasKilled = false
				}
			}
		}
	}

	fun destroyNPC(npc: NPC): CompletableFuture<Unit> =
		npc.entity.location.world.getChunkAtAsync(npc.entity.location).thenApply { _ ->
			npc.entity.chunk.removePluginChunkTicket(IonServer)

			npc.destroy()
			CitizensAPI.getNPCRegistry().deregister(npc)
		}
}

class CombatNPCKillEvent(val id: UUID, val name: String, val killer: Entity?) : Event() {
	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList = handlerList
}
