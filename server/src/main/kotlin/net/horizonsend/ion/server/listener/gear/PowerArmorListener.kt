package net.horizonsend.ion.server.listener.gear

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.POWER_STORAGE
import net.horizonsend.ion.server.features.custom.items.type.armor.PowerArmorItem
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.glideDisabledPlayers
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.setGliding
import net.horizonsend.ion.server.features.gear.removePower
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

private val lastMoved = HashMap<UUID, Long>()

fun hasMovedInLastSecond(player: Player): Boolean {
	return lastMoved.containsKey(player.uniqueId) && Instant.now().toEpochMilli() - (lastMoved[player.uniqueId] ?: 0) < 1000
}

object PowerArmorListener : SLEventListener() {
	@EventHandler
	fun onEquipPowerArmor(event: PlayerArmorChangeEvent) {
		val player: Player = event.player
		val slot: PlayerArmorChangeEvent.SlotType = event.slotType

		Tasks.sync {
//			if (!player.isOnline) {
//				return@sync
//			}
//
//			val item: ItemStack = player.inventory.armorContents[3 - slot.ordinal] ?: return@sync
//			val customItem: CustomItems.PowerArmorItem = CustomItems[item] as? CustomItems.PowerArmorItem ?: return@sync
//
//			val meta = item.itemMeta as LeatherArmorMeta
//			if (meta.displayName != customItem.displayName) {
//				return@sync
//			}
//
//			val nation = PlayerCache[player].nationOid?.let(NationCache::get) ?: return@sync
//			val nationColor = nation.color
//
//			if (meta.color.asRGB() == nationColor) {
//				return@sync
//			}
//
//			val bukkitColor: Color = Color.fromRGB(nationColor)
//			meta.setColor(bukkitColor)
//			item.itemMeta = meta
//			player.updateInventory()
//			player action "&7&oPower armor color changed to match nation color (rename it in an anvil to fix this)"
		}
	}

	@EventHandler
	fun onEntityDamage(event: EntityDamageEvent) {
		if (event.entity !is Player) return
		val player = event.entity as Player
		var modifier = 0.0
		val modules = HashMap<ItemModification, ItemStack>()
		val cause = event.cause

		for (item in player.inventory.armorContents) {
			val customItem = item?.customItem ?: continue
			if (customItem !is PowerArmorItem) continue
			if (customItem.hasComponent(POWER_STORAGE)) return continue
			val powerStorage = customItem.getComponent(POWER_STORAGE)
			val power = powerStorage.getPower(item)

			if (power < 100) continue

			if (item.enchantments.none()) {
				modifier += 0.5 / 4
			}

			if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
				!player.world.hasFlag(WorldFlag.ARENA) &&
				!ProtectionListener.isProtectedCity(player.location)
			) {
				powerStorage.removePower(item, customItem, 100)
			}

			for (module in customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getMods(item)) {
				modules[module] = item
			}
		}

		for ((_, moduleItem) in modules) {
			if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
				modifier = 0.0
				if (!player.world.hasFlag(WorldFlag.ARENA)) {
					removePower(moduleItem, 10)
				}
			}
		}

		if (modifier == 0.0) {
			return
		}

		if (!event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
			return
		}

		event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, -event.damage * modifier)
	}

	@EventHandler
	fun onMove(event: PlayerMoveEvent) {
		lastMoved[event.player.uniqueId] = Instant.now().toEpochMilli()
	}

	@EventHandler
	fun onToggleRocketBoosters(event: PlayerToggleSneakEvent) {
		val player = event.player
		if (ActiveStarships.findByPilot(player) != null && player.inventory.itemInMainHand.type == Material.CLOCK) return

		val boots = event.player.inventory.boots ?: return
		val customItem = boots.customItem
		if (customItem !is PowerArmorItem) return

		val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getMods(boots)
		if (!mods.contains(ItemModRegistry.ROCKET_BOOSTING)) return

		val power = customItem.getComponent(POWER_STORAGE).getPower(boots)
		if (power <= 0) return

		setGliding(player, true)
	}

	@EventHandler
	fun onEntityKnockBackEvent(event: EntityKnockbackByEntityEvent) {
		val player = event.entity as? Player ?: return

		for (item in player.inventory.armorContents) {
			val customItem = item?.customItem ?: continue

			if (customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) return continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getMods(item)

			if (!mods.contains(ItemModRegistry.SHOCK_ABSORBING)) return continue

			if (customItem.hasComponent(POWER_STORAGE)) return continue
			val power = customItem.getComponent(POWER_STORAGE).getPower(item)

			if (power <= 0) continue

			event.isCancelled = true
			return
		}
	}

	@EventHandler
	fun onEntityToggleGlideEvent(event: EntityToggleGlideEvent) {
		val player = event.entity as? Player ?: return
		if (player.isGliding && player.isSneaking && glideDisabledPlayers[player.uniqueId] == null) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onPlayerRocketBootDamage(event: EntityDamageEvent) {
		if (event.entity !is Player) return
		if (event.cause != EntityDamageEvent.DamageCause.FLY_INTO_WALL) return

		event.isCancelled = true
	}
}
