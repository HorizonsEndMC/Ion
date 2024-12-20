package net.horizonsend.ion.server.features.player

import net.citizensnpcs.api.event.NPCDamageByEntityEvent
import net.citizensnpcs.api.event.NPCDamageEvent
import net.citizensnpcs.api.event.NPCDeathEvent
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.Gravity
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.npcs.NPCManager
import net.horizonsend.ion.server.features.npcs.isCitizensLoaded
import net.horizonsend.ion.server.features.npcs.traits.CombatNPCTrait
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
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
import org.litote.kmongo.pull
import org.litote.kmongo.push
import org.litote.kmongo.setValue
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object CombatNPCs : IonServerComponent(true) {
	val manager = NPCManager(log, "CombatNPCs")

	override fun onEnable() {
		if (!isCitizensLoaded) return

		// Do not register the listeners if not enabled
		if (!ConfigurationFiles.featureFlags().combatNPCs) return

		manager.enableRegistry()

		// weirdness happens when someone already logged in logs on. this is my hacky fix.
		val lastJoinMap = mutableMapOf<UUID, Long>()

		// when a player joins, remove their combat npc
		listen<PlayerJoinEvent>(EventPriority.LOWEST) { event ->
			val player = event.player
			val playerId: UUID = player.uniqueId

			lastJoinMap[playerId] = System.currentTimeMillis()
		}

		//when a player quits, create a combat npc
		listen<PlayerQuitEvent> { event ->
			val player = event.player
			val playerId = player.uniqueId

			// if this permission is granted, do not spawn the npc
			if (player.hasPermission("starlegacy.combatnpc.bypass")) return@listen

			val inventoryCopy: Array<ItemStack?> = player.inventory.contents
				.map { item: ItemStack? -> item?.clone() }
				.toTypedArray()

			manager.createNPC(
				player.name,
				UUID.randomUUID(),
				3000 + manager.allNPCs().size,
				player.location
			) { npc ->
				npc.entity.customName(text("${player.name} [NPC]"))
				npc.isProtected = false

				npc.getOrAddTrait(Gravity::class.java).apply {
					setEnabled(true) // nogravity = true
				}

				npc.getOrAddTrait(CombatNPCTrait::class.java).apply {
					owner = event.player.uniqueId
					inventoryContents = inventoryCopy
					despawnTime = if (CombatTimer.isPvpCombatTagged(event.player))
						CombatTimer.pvpTimerRemainingMillis(event.player).coerceAtLeast(TimeUnit.MINUTES.toMillis(4L)) + System.currentTimeMillis()
					else TimeUnit.MINUTES.toMillis(4L) + System.currentTimeMillis()
					wasInCombat = CombatTimer.isPvpCombatTagged(event.player)
				}
			}
		}

		listen<NPCDamageByEntityEvent>(EventPriority.LOWEST) { event ->
			if (event.damager !is Player) {
				event.isCancelled = true
			}
		}

		listen<NPCDamageEvent>(EventPriority.LOWEST) { event ->
			if (acceptableCauses.contains(event.cause)) return@listen

			event.isCancelled = true
		}

		listen<NPCDeathEvent>(EventPriority.LOWEST) { event ->
			println("NPC DIED")
			val npc = event.npc
			val trait = npc.getTraitNullable(CombatNPCTrait::class.java) ?: return@listen

			val playerId = trait.owner

			val entity = npc.entity
			entity.chunk.removePluginChunkTicket(IonServer)

			val killer: Entity? = (entity.lastDamageCause as? EntityDamageByEntityEvent)?.damager

			if (trait.isExpired()) {
				killer?.userError("Combat NPC expired!")

				npc.destroy()
				return@listen
			}

			val drops: Array<ItemStack?> = trait.inventoryContents

			Bukkit.getPlayer(playerId)?.let { onlinePlayer ->
				log.warn("NPC for ${onlinePlayer.name} was killed while they were online!")
				return@listen
			}

			event.drops.addAll(drops)

			destroyNPC(npc)

			Tasks.async {
				SLPlayer.updateById(playerId.slPlayerId, push(SLPlayer::wasKilledOn, ConfigurationFiles.serverConfiguration().serverName))

				val name: String = SLPlayer.getName(playerId.slPlayerId) ?: "UNKNOWN"
				Notify.chatAndEvents(miniMessage.deserialize("<red>Combat NPC of $name was slain by ${killer?.name}"))

				Tasks.sync {
					CombatNPCKillEvent(playerId, name, killer).callEvent()
				}
			}
		}

		// Handle logins after npc killed
		listen<PlayerJoinEvent> { event ->
			if (SLPlayer[event.player].wasKilledOn.contains(ConfigurationFiles.serverConfiguration().serverName)) {
				event.player.inventory.clear()
				event.player.health = 0.0
				event.player.alert("Your NPC was killed while you were offline.")
			}
		}

		// Handle deaths from having npc killed
		listen<PlayerDeathEvent>(priority = EventPriority.LOWEST) { event ->
			if (event.player.hasMetadata("NPC")) return@listen

			val data = SLPlayer[event.player]
			if (data.wasKilledOn.contains(ConfigurationFiles.serverConfiguration().serverName)) {
				event.drops.clear()
				event.deathMessage(null)

				SLPlayer.updateById(event.player.slPlayerId, pull(SLPlayer::wasKilledOn, ConfigurationFiles.serverConfiguration().serverName))
				SLPlayer.updateById(data._id, setValue(SLPlayer::wasKilled, false))
			}
		}
	}

	private val acceptableCauses: EnumSet<EntityDamageEvent.DamageCause> = EnumSet.of(
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

	override fun onDisable() {
		if (!ConfigurationFiles.featureFlags().combatNPCs) return // Not enabled

		manager.disableRegistry()
	}

	fun destroyNPC(npc: NPC): CompletableFuture<Unit> = npc.storedLocation.world.getChunkAtAsync(npc.storedLocation).thenApply { _ ->
		manager.removeNPC(npc.uniqueId)

		npc.storedLocation.chunk.removePluginChunkTicket(IonServer)
	}

	val EXPIRED_NPC_CUTOFF get() = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(4)
}

class CombatNPCKillEvent(val id: UUID, val name: String, val killer: Entity?) : Event() {
	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList = handlerList
}
