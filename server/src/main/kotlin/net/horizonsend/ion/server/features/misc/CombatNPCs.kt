package net.horizonsend.ion.server.features.misc

import net.citizensnpcs.api.event.NPCDamageByEntityEvent
import net.citizensnpcs.api.event.NPCDamageEvent
import net.citizensnpcs.api.event.NPCDeathEvent
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.createNamedMemoryRegistry
import net.horizonsend.ion.server.miscellaneous.utils.firsts
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.horizonsend.ion.server.miscellaneous.utils.isNPC
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.litote.kmongo.setValue
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.CompletableFuture

object CombatNPCs : IonServerComponent(true) {
	private const val remainTimeMinutes = 4L

	/** Map of NPC ID to its inventory */
	private val inventories: MutableMap<Int, Array<ItemStack?>> = mutableMapOf()
	val npcToPlayer = mutableMapOf<UUID, Pair<NPC, Location>>()

	private lateinit var combatNpcRegistry: NPCRegistry

	override fun onEnable() {
		// weirdness happens when someone already logged in logs on. this is my hacky fix.
		val lastJoinMap = mutableMapOf<UUID, Long>()

		// when a player joins, remove their combat npc
		listen<PlayerJoinEvent>(EventPriority.LOWEST) { event ->
			val player = event.player
			val playerId: UUID = player.uniqueId

			lastJoinMap[playerId] = System.currentTimeMillis()
		}

		combatNpcRegistry = createNamedMemoryRegistry("combat-npcs")

		//when a player quits, create a combat npc
		listen<PlayerQuitEvent> { event ->
			if (IonServer.configuration.serverName == "Creative") return@listen

			val player = event.player
			val playerId = player.uniqueId

			// if this permission is granted, do not spawn the npc
			if (player.hasPermission("starlegacy.combatnpc.bypass")) {
				return@listen
			}

			// if they joined less than 10 seconds ago, don't do it
			if (System.currentTimeMillis() - (lastJoinMap[playerId] ?: 0) < 10000) {
				return@listen
			}

			val inventoryCopy: Array<ItemStack?> = player.inventory.contents
				.map { item: ItemStack? -> item?.clone() }
				.toTypedArray()

			val location = player.location

			val npc = combatNpcRegistry.createNPC(EntityType.PLAYER, player.name, player.location)

			npc.entity.customName(text("${player.name} [NPC]"))
			npc.isProtected = false
			npc.spawn(player.location)

			inventories[npc.id] = inventoryCopy
			npcToPlayer[playerId] = npc to location

			npc.entity.persistentDataContainer.set(
				NamespacedKeys.COMBAT_NPC,
				PersistentDataType.LONG,
				System.currentTimeMillis()
			)

			npc.entity.chunk.addPluginChunkTicket(IonServer)

			Tasks.syncDelay(20L * 60L * remainTimeMinutes) {
				if (!npc.isSpawned) {
					location.chunk.removePluginChunkTicket(IonServer)
					return@syncDelay
				}

				inventories.remove(npc.id)
				npcToPlayer.remove(playerId)

				location.chunk.removePluginChunkTicket(IonServer)
				destroyNPC(npc)
			}
		}

		listen<NPCDamageByEntityEvent>(EventPriority.LOWEST) { event ->
			if (event.damager is Player) return@listen

			event.isCancelled = true
		}

		val acceptableCauses: EnumSet<EntityDamageEvent.DamageCause> = EnumSet.of(
			EntityDamageEvent.DamageCause.ENTITY_ATTACK,
			EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
			EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
			EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
			EntityDamageEvent.DamageCause.FIRE,
			EntityDamageEvent.DamageCause.FIRE_TICK,
			EntityDamageEvent.DamageCause.PROJECTILE,
			EntityDamageEvent.DamageCause.SUICIDE,
			EntityDamageEvent.DamageCause.CUSTOM,
			EntityDamageEvent.DamageCause.LAVA,
		)

		listen<NPCDamageEvent>(EventPriority.LOWEST) { event ->
			if (!acceptableCauses.contains(event.cause)) return@listen

			event.isCancelled = true
		}

		listen<NPCDeathEvent>(EventPriority.LOWEST) { event ->
			val npc = event.npc
			val playerId = npcToPlayer.entries.find { it.value.first.id == npc.id }?.key ?: return@listen

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

			SLPlayer.updateById(playerId.slPlayerId, setValue(SLPlayer::wasKilled, true))
			Tasks.async {
				val name: String = SLPlayer.getName(playerId.slPlayerId) ?: "UNKNOWN"
				Notify.all(
					MiniMessage.miniMessage().deserialize("<red>Combat NPC of $name was slain by ${killer?.name}")
				)
				Tasks.sync {
					CombatNPCKillEvent(playerId, name, killer).callEvent()
				}
			}
		}

		listen<PlayerJoinEvent> { event ->
			npcToPlayer[event.player.uniqueId]?.let {
				it.second.chunk.removePluginChunkTicket(IonServer)
				destroyNPC(it.first)
			}

			if (SLPlayer[event.player].wasKilled) {
				event.player.inventory.clear()
				event.player.health = 0.0
				event.player.alert("Your NPC was killed while you were offline.")
			}
		}

		listen<PlayerDeathEvent>(priority = EventPriority.LOWEST) { event ->
			if (event.player.isNPC()) return@listen
			val data = SLPlayer[event.player]
			if (data.wasKilled) {
				event.drops.clear()
				event.deathMessage(null)

				SLPlayer.updateById(data._id, setValue(SLPlayer::wasKilled, false))
			}
		}
	}

	override fun onDisable() {
		npcToPlayer.values.firsts().forEach(CombatNPCs::destroyNPC)
	}

	fun destroyNPC(npc: NPC): CompletableFuture<Unit> =
		npc.storedLocation.world.getChunkAtAsync(npc.storedLocation).thenApply { _ ->
			npc.storedLocation.chunk.removePluginChunkTicket(IonServer)

			npc.destroy()
			combatNpcRegistry.deregister(npc)
		}

	/** Bukkit treats NPCs as Player **/
	fun Player.isCombatNpc() : Boolean {
		return combatNpcRegistry.isNPC(this)
	}
}

class CombatNPCKillEvent(val id: UUID, val name: String, val killer: Entity?) : Event() {
	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList = handlerList
}
