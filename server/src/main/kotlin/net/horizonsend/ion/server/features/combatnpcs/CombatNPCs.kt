package net.horizonsend.ion.server.features.combatnpcs

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.extensions.information
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class CombatNPCs : Listener {

	// CREDITS TO TRAINBOY
	// Dead player to killer
	private val deadPlayers = mutableMapOf<UUID, UUID?>()

	@EventHandler
	fun onPlayerLogOut(event: PlayerQuitEvent) {
		val player = event.player
		if (player.gameMode != GameMode.SURVIVAL) return
		// Have to store the inventory as it isn't accessible in OfflinePlayer
		Tasks.syncDelay(20 * 5) {
			if (player.isOnline) return@syncDelay

			val stand =
				player.world.spawn(player.location, ArmorStand::class.java).apply {
					customName = player.name
					isCustomNameVisible = true

					setArms(true)
					setGravity(false)
					setCanMove(false)

					// copy inventory
					val inventory = player.inventory
					equipment.armorContents = player.equipment.armorContents
					equipment.setItemInMainHand(inventory.itemInMainHand)
					setItem(EquipmentSlot.HAND, inventory.itemInMainHand)

					// set skull
					equipment.helmet = ItemStack(Material.PLAYER_HEAD, 1).apply {
						itemMeta = (itemMeta as SkullMeta).apply {
							playerProfile = player.playerProfile
						}
					}

					val playerMaxHealth: Double = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
					getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = playerMaxHealth
					health = player.health

					removeWhenFarAway = false
				}

			val chunk = player.chunk.apply { addPluginChunkTicket(Ion) }
			stands[stand] = Pair(player.uniqueId, player.inventory)

			Tasks.syncDelay(1200) {
				stands[stand] ?: return@syncDelay
				stands.remove(stand)
				stand.health = 0.0
				chunk.removePluginChunkTicket(Ion)
			}
		}
	}

	@EventHandler
	fun onPlayerLogIn(event: PlayerJoinEvent) {
		// If the player has an armor stand, despawn it
		// Note this would only remove one, if somehow they got multiple that would be an issue
		val possibleStand = stands.filter { it.value.first == event.player.uniqueId }.keys.firstOrNull()
		if (possibleStand != null) {
			stands.remove(possibleStand)
			possibleStand.remove()
			event.player.chunk.removePluginChunkTicket(Ion)
		}

		if (!deadPlayers.containsKey(event.player.uniqueId)) return

		// Kill the player, send a message, and clear their inventory
		val killer = run { Bukkit.getOfflinePlayer(deadPlayers[event.player.uniqueId] ?: return@run null) }
		event.player.information("While you were offline you were killed by ${killer?.name}")
		deadPlayers.remove(event.player.uniqueId)
		event.player.inventory.clear()
		event.player.health = 0.0
	}

	@EventHandler
	fun onArmorStandDie(event: EntityDeathEvent) {
		val stand = event.entity as? ArmorStand ?: return
		val player = Bukkit.getOfflinePlayer(stands[stand]?.first ?: return)
		val inventory = stands[stand]!!.second
		val location = event.entity.location

		inventory.filter { !(it?.containsEnchantment(Enchantment.VANISHING_CURSE) ?: true) }.forEach {
			location.world.dropItem(location, it)
		}

		Bukkit.getPluginManager().callEvent(CombatNPCKillEvent(player.uniqueId, player.name!!, stand.killer))

		stands.remove(stand)
		stand.chunk.removePluginChunkTicket(Ion)
		deadPlayers[player.uniqueId] = (stand.lastDamageCause as? EntityDamageByEntityEvent)?.damager?.uniqueId
		event.drops.clear() // don't want to drop the stand
	}

	companion object {
		private val stands = mutableMapOf<ArmorStand, Pair<UUID, PlayerInventory>>()
		fun isNPC(armorStand: ArmorStand): Boolean = stands.contains(armorStand)
	}
}

class CombatNPCKillEvent(val killedUuid: UUID, val killedName: String, val killer: Player?) : Event() {
	override fun getHandlers(): HandlerList = handlerList
	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
