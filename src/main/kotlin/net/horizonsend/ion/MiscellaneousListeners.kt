package net.horizonsend.ion

import org.bukkit.Material
import org.bukkit.Material.BLACK_CONCRETE_POWDER
import org.bukkit.Material.BLUE_CONCRETE_POWDER
import org.bukkit.Material.BROWN_CONCRETE_POWDER
import org.bukkit.Material.CYAN_CONCRETE_POWDER
import org.bukkit.Material.GRAY_CONCRETE_POWDER
import org.bukkit.Material.GREEN_CONCRETE_POWDER
import org.bukkit.Material.ICE
import org.bukkit.Material.LIGHT_BLUE_CONCRETE_POWDER
import org.bukkit.Material.LIGHT_GRAY_CONCRETE_POWDER
import org.bukkit.Material.LIME_CONCRETE_POWDER
import org.bukkit.Material.MAGENTA_CONCRETE_POWDER
import org.bukkit.Material.ORANGE_CONCRETE_POWDER
import org.bukkit.Material.PINK_CONCRETE_POWDER
import org.bukkit.Material.PURPLE_CONCRETE_POWDER
import org.bukkit.Material.RED_CONCRETE_POWDER
import org.bukkit.Material.WHITE_CONCRETE_POWDER
import org.bukkit.Material.YELLOW_CONCRETE_POWDER
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.PATROL
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.RAID
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

class MiscellaneousListeners: Listener {
	private val concretePowder = enumSetOf(
		WHITE_CONCRETE_POWDER,
		ORANGE_CONCRETE_POWDER,
		MAGENTA_CONCRETE_POWDER,
		LIGHT_BLUE_CONCRETE_POWDER,
		YELLOW_CONCRETE_POWDER,
		LIME_CONCRETE_POWDER,
		PINK_CONCRETE_POWDER,
		GRAY_CONCRETE_POWDER,
		LIGHT_GRAY_CONCRETE_POWDER,
		CYAN_CONCRETE_POWDER,
		PURPLE_CONCRETE_POWDER,
		BLUE_CONCRETE_POWDER,
		BROWN_CONCRETE_POWDER,
		GREEN_CONCRETE_POWDER,
		RED_CONCRETE_POWDER,
		BLACK_CONCRETE_POWDER
	)

	@EventHandler
	fun onConcreteHarden(event: BlockFormEvent) {
		if (concretePowder.contains(event.block.type)) event.isCancelled = true
	}

	@EventHandler
	fun onPrepareItemEnchantEvent(event: PrepareItemEnchantEvent) {
		event.offers!![0] = EnchantmentOffer(Enchantment.SILK_TOUCH, 1, 120)
		event.offers!![1] = null
		event.offers!![2] = null
	}

	@EventHandler
	fun onPrepareAnvilEvent(event: PrepareAnvilEvent) {
		if (event.inventory.firstItem == null) return
		if (event.inventory.secondItem == null) return

		if (event.inventory.secondItem!!.type == Material.ENCHANTED_BOOK) {
			if (!event.inventory.secondItem!!.enchantments.containsKey(Enchantment.SILK_TOUCH)) event.result = null
			else {
				event.result = event.inventory.firstItem!!.clone()
				event.result!!.enchantments[Enchantment.SILK_TOUCH] = 1
			}
		}
	}

	@EventHandler
	fun onIceMelt(event: BlockFadeEvent) {
		if (event.block.type == ICE) event.isCancelled = true
	}

	private val canceledSpawnReasons= enumSetOf(
		NATURAL,
		RAID,
		REINFORCEMENTS,
		PATROL
	)

	@EventHandler
	fun onMobSpawn(event: CreatureSpawnEvent) {
		if (canceledSpawnReasons.contains(event.spawnReason)) event.isCancelled = true
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) = event.joinMessage(null)

	@EventHandler
	fun onPlayerQuit(event: PlayerQuitEvent) = event.quitMessage(null)

	@EventHandler
	fun onPlayerTeleport(event: PlayerTeleportEvent) {
		event.isCancelled = when(event.cause) {
			TeleportCause.CHORUS_FRUIT -> true
			TeleportCause.ENDER_PEARL -> true
			else -> false
		}
	}
}