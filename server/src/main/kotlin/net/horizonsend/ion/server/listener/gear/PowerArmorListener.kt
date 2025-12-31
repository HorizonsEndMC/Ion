package net.horizonsend.ion.server.listener.gear

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import io.papermc.paper.event.entity.EntityMoveEvent
import io.papermc.paper.event.player.PlayerArmSwingEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import io.papermc.paper.registry.keys.AttributeKeys.MAX_HEALTH
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.starship.StarshipSounds
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.MOD_MANAGER
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.POWER_STORAGE
import net.horizonsend.ion.server.features.custom.items.type.armor.PowerArmorItem
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.ArmorLockMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.ArmorLockMod.setLocked
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.GravityFieldMod.setGravity
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.HoverMod.setHover
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.glideDisabledPlayers
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.setGliding
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Blaster
import net.horizonsend.ion.server.features.explosions.presets.MiniNukeModExplosion
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.minecraft.sounds.SoundEvents.TOTEM_USE
import net.minecraft.util.datafix.fixes.EntityHealthFix
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.RESISTANCE
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
		val modules = HashMap<IonRegistryKey<ItemModification, out ItemModification>, ItemStack>()
		val cause = event.cause

		for (item in player.inventory.armorContents) {
			val customItem = item?.customItem ?: continue
			if (customItem !is PowerArmorItem) continue
			if (!customItem.hasComponent(POWER_STORAGE)) return continue
			val powerStorage = customItem.getComponent(POWER_STORAGE)
			val power = powerStorage.getPower(item)

			if (power < 1) continue

			if (item.enchantments.none()) {
				modifier += 0.5 / 4
			}

			if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
				!player.world.hasFlag(WorldFlag.ARENA) &&
				!ProtectionListener.isProtectedCity(player.location)
			) {
				powerStorage.removePower(item, customItem, 0)
			}

			for (module in customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)) {
				modules[module] = item
			}
		}

		for ((_, moduleItem) in modules) {
			if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
				modifier = 0.0
				if (!player.world.hasFlag(WorldFlag.ARENA)) {
					val customItem = moduleItem.customItem ?: continue
					customItem.getComponent(POWER_STORAGE).removePower(moduleItem, customItem, 0)
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

		val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(boots)
		if (!mods.contains(ItemModKeys.ROCKET_BOOSTING)) return

		val power = customItem.getComponent(POWER_STORAGE).getPower(boots)
		if (power <= 0) return

		setGliding(player, true)
	}

/*	@EventHandler
	fun onEntityKnockBackEvent(event: EntityKnockbackByEntityEvent) {
		val player = event.entity as? Player ?: return

		for (item in player.inventory.armorContents) {
			val customItem = item?.customItem ?: continue

			if (customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) return continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)

			if (!mods.contains(ItemModKeys.SHOCK_ABSORBING)) return continue

			if (customItem.hasComponent(POWER_STORAGE)) return continue
			val power = customItem.getComponent(POWER_STORAGE).getPower(item)

			if (power <= 0) continue

			event.isCancelled = true
			return
		}
	} */

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

	@EventHandler
	fun onPlayerDeath(event: EntityDeathEvent) {
		if (event.entity !is Player) return
		for (item in (event.entity as Player).inventory.armorContents) {
			val customItem = item?.customItem ?: continue

			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.MINI_NUKE)) continue

			val location = event.entity.location
			MiniNukeModExplosion(location).spawnExplosion(event.entity as Player)
			return
		}
	}

	@EventHandler
	fun onSavePlayer(event: EntityDamageEvent) {
		if (event.entity !is Player) return
		for (item in (event.entity as Player).inventory.armorContents) {
			val customItem = item?.customItem ?: continue

			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.PALADIN)) continue
			if (!(event.finalDamage > (event.entity as Player).maxHealth*0.9)) return
			event.setDamage(1.0)
			(event.entity as Player).health = 10.0
			event.entity.world.playSound(event.entity.location, Sound.ITEM_TOTEM_USE, 1.0f ,1.0f)
			return
		}
	}
	//Stop SMBs sending people flying
	@EventHandler
	fun onSMGHit(event: EntityDamageByEntityEvent) {
		if (event.entity !is Player) return
		if (event.damager !is Player) return
		val damager = (event.damager as Player)
		val damaged = (event.entity as Player)
		val customItem = damager.inventory.itemInMainHand.customItem ?: return
		if (customItem !is Blaster<*>) return
		if (customItem.identifier != "SUBMACHINE_BLASTER") return
		event.isCancelled = true
		damaged.damage(event.damage)
	}

	@EventHandler
	fun onPlayerLowHealth(event: PrePlayerAttackEntityEvent) {
		if (event.attacked !is Player) return
		val player = event.attacked as Player
		if (player.health > (player.maxHealth * 0.25)) return
		for (item in (event.attacked as Player).inventory.armorContents) {
			val customItem = item?.customItem ?: continue

			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.GUARDIAN)) continue
			val potionEffect = PotionEffect(RESISTANCE, 40, 0)
			player.addPotionEffect(potionEffect)
			return
		}
	}

	@EventHandler
	fun damageDebug(event: EntityDamageByEntityEvent) {
		if (event.entity !is Player) return
		if (event.damager !is Player) return
		val damager = event.damager as Player
		event.entity.debug("damage: ${event.finalDamage}")
		event.entity.debug("item in hand: ${damager.inventory.itemInMainHand}")
	}

	@EventHandler
	fun onPlayerKill(event: EntityDeathEvent) {
		if (event.entity !is Player) return
		if (event.damageSource.causingEntity !is Player) return
		for (item in (event.damageSource.causingEntity as Player).inventory.armorContents) {
			val customItem = item?.customItem ?: continue

			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.SIPHON)) continue
			(event.damageSource.causingEntity as Player).heal(15.0)
			return
		}
	}

	@EventHandler
	fun onPlayerGravityAttempt(event: PlayerToggleSneakEvent) {
		if (event.player.isSneaking) return
		for (item in event.player.inventory.armorContents) {
			val customItem = item?.customItem ?: continue
			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.GRAVITY_FIELD)) continue
			return setGravity(event.player)
		}
	}

	@EventHandler
	fun onPlayerHoverAttempt(event: PlayerToggleSneakEvent) {
		if (event.player.isSneaking) return
		if (event.player.isOnGround) return
		for (item in event.player.inventory.armorContents) {
			val customItem = item?.customItem ?: continue
			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.HOVER)) continue
			return setHover(event.player)
		}
	}

	@EventHandler
	fun onPlayerLockAttempt(event: PlayerInteractEvent) {
		if (!(!event.action.isRightClick && event.player.isSneaking && event.player.isOnGround && event.player.inventory.itemInMainHand.type == Material.AIR)) return
		for (item in event.player.inventory.armorContents) {
			val customItem = item?.customItem ?: continue
			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.ARMOR_LOCK)) continue
			return setLocked(event.player)
		}
	}

	//LISTENERS FOR ARMOUR LOCK
	@EventHandler
	fun onPlayerRegen(event: EntityRegainHealthEvent) {
		if (event.entity !is Player) return
		if (ArmorLockMod.armorLockEnabledPlayers.contains(event.entity.uniqueId)) event.isCancelled = true
		return
	}
	@EventHandler
	fun onPlayerJump(event: PlayerJumpEvent) {
		if (ArmorLockMod.armorLockEnabledPlayers.contains(event.player.uniqueId)) event.isCancelled = true
		return
	}
	@EventHandler
	fun onPlayerSwing(event: PlayerArmSwingEvent) {
		if (ArmorLockMod.armorLockEnabledPlayers.contains(event.player.uniqueId)) event.isCancelled = true
		return
	}
	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		if (ArmorLockMod.armorLockEnabledPlayers.contains(event.player.uniqueId)) event.isCancelled = true
		return
	}
	@EventHandler
	fun onPlayerWalk(event: PlayerMoveEvent) {
		if (ArmorLockMod.armorLockEnabledPlayers.contains(event.player.uniqueId)) event.isCancelled = true
		return
	}
}
