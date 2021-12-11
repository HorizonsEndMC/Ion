package net.starlegacy.listener.gear

import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.Tasks
import net.starlegacy.util.msg
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Vex
import org.bukkit.entity.Vindicator
import org.bukkit.entity.WitherSkeleton
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent


object SwordListener : SLEventListener() {
    @EventHandler
    fun onSwordBreakBlockCreative(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_BLOCK) {
            return
        }

        val player = event.player
        if (player.gameMode != GameMode.CREATIVE) {
            return
        }

        val customItem = CustomItems[event.item] ?: return
        if (isSword(customItem)) {
            event.isCancelled = true
        }
    }

    private fun isSword(customItem: CustomItem) = customItem is CustomItems.EnergySwordItem

    @EventHandler
    fun onSlashWithSword(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_BLOCK && event.action != Action.LEFT_CLICK_AIR) return
        val player = event.player
        val sword = player.inventory.itemInMainHand
        val customItem = CustomItems[sword] ?: return
        if (isSword(customItem)) player.world.playSound(player.location, "energy_sword.swing", 1.0f, 1.0f)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onHitWithContrivance(event: EntityDamageByEntityEvent) {
        val damaged = event.entity
        if (damaged is HumanEntity && damaged.isBlocking) {
            if (damaged.getCooldown(Material.SHIELD) == 0) {
                val velocity = damaged.getVelocity()

                Tasks.syncDelay(1) { damaged.velocity = velocity }

                event.damage = 0.0
                damaged.setCooldown(Material.SHIELD, 15)
                damaged.arrowsStuck = 0
                damaged.world.playSound(damaged.location, "energy_sword.strike", 5.0f, 1.0f)
                return
            } else {
                event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0.0)
            }
        }
        val damager = event.damager as? LivingEntity ?: return
        val itemInHand = damager.equipment?.itemInMainHand ?: return
        val customItem = CustomItems[itemInHand] ?: return

        if (!isSword(customItem) || event.getDamage(EntityDamageEvent.DamageModifier.BASE) < 1.0f) {
            return
        }

        event.setDamage(EntityDamageEvent.DamageModifier.BASE, 8.0)
        damaged.world.playSound(damaged.location, "energy_sword.strike", 1.0f, 1.0f)
    }

    @EventHandler
    fun onZombieSpawn(event: CreatureSpawnEvent) {
        if (event.entityType != EntityType.ZOMBIE) return
        val zombie = event.entity as Zombie
        if (zombie.world.name.toLowerCase().contains("arena")) Tasks.sync {
            zombie.equipment?.setItemInMainHand(CustomItems["energy_sword_purple"]?.itemStack(1))
        }
    }

    @EventHandler
    fun onVindicatorSpawn(event: CreatureSpawnEvent) {
        if (event.entityType != EntityType.VINDICATOR) return
        val vindicator = event.entity as Vindicator

        if (vindicator.world.name.toLowerCase().contains("arena")) Tasks.sync {
            vindicator.equipment?.setItemInMainHand(CustomItems["energy_sword_green"]?.itemStack(1))
        }
    }

    @EventHandler
    fun onVexSpawn(event: CreatureSpawnEvent) {
        if (event.entityType != EntityType.VEX) return
        val vex = event.entity as Vex

        if (vex.world.name.toLowerCase().contains("arena")) Tasks.sync {
            vex.equipment?.setItemInMainHand(CustomItems["energy_sword_blue"]?.itemStack(1))
        }
    }

    @EventHandler
    fun onWitherskeletonSpawn(event: CreatureSpawnEvent) {
        if (event.entityType != EntityType.WITHER_SKELETON) return
        val witherskeleton = event.entity as WitherSkeleton

        if (witherskeleton.world.name.toLowerCase().contains("arena")) Tasks.sync {
            witherskeleton.equipment?.setItemInMainHand(CustomItems["energy_sword_red"]?.itemStack(1))
        }
    }

    @EventHandler
    fun onCraftSword(event: PrepareItemCraftEvent) {
        val item = CustomItems[event.inventory.result ?: return] as? CustomItems.EnergySwordItem ?: return
        val permission = "gear.energysword." + item.id.removePrefix("energy_sword_")
        if (!event.view.player.hasPermission(permission)) {
            event.view.player msg "&cYou can only craft yellow energy swords unless you donate for other colors!"
            event.inventory.result = null
        }
    }
}
