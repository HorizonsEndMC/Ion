package net.starlegacy.listener.gear

import net.starlegacy.feature.gear.blaster.Blasters
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.Tasks
import org.bukkit.DyeColor
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Skeleton
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action.LEFT_CLICK_AIR
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Colorable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.Locale

object BlasterListener : SLEventListener() {
	@EventHandler
	fun onClick(event: PlayerInteractEvent) {
		if (event.action != LEFT_CLICK_AIR && event.action != LEFT_CLICK_BLOCK) {
			return
		}

		val item = event.item ?: return
		val blaster = Blasters.getBlaster(item) ?: return
		event.isCancelled = true
		val player = event.player

		Blasters.fireBlaster(player, item, Blasters.getBlasterType(blaster))

		player.addPotionEffect(
			PotionEffect(PotionEffectType.FAST_DIGGING, 10, 5)
		)
	}

	@EventHandler
	fun onFireBlaster(event: EntityShootBowEvent) {
		val entity = event.entity
		val bow = event.bow ?: return

		if (entity is Player) {
			return
		}

		val blaster = Blasters.getBlaster(bow) ?: return

		event.isCancelled = true
		Blasters.fireBlaster(entity, bow, Blasters.getBlasterType(blaster))
	}

	@EventHandler
	fun preCraft(event: PrepareItemCraftEvent) {
		var color: DyeColor? = null
		var dye: ItemStack? = null
		for (item: ItemStack? in event.inventory.matrix!!) {
			if (item != null && item.data is Colorable) {
				color = (item.data as Colorable).color
				dye = item
				break
			}
		}

		if (dye == null || color == null) {
			return
		}

		for (item: ItemStack? in event.inventory.matrix!!) {
			if (item == null) {
				continue
			}

			Blasters.getBlaster(item) ?: continue
			val lore = item.lore ?: mutableListOf()

			if (lore.size < 2) {
				lore.add(color.name)
			} else {
				lore[1] = color.name
			}

			if (lore == item.lore) {
				return
			}

			if (item.lore == lore) {
				return
			}

			item.lore = lore
			dye.amount = dye.amount - 1
			return
		}
	}

	@EventHandler
	fun onSkeletonSpawn(event: CreatureSpawnEvent) {
		if (event.entityType != EntityType.SKELETON) return
		val skeleton = event.entity as Skeleton

		if (skeleton.world.name.lowercase(Locale.getDefault()).contains("arena")) {
			Tasks.sync {
				val blasterRifle = CustomItems["blaster_rifle"]?.itemStack(1)
				val meta = blasterRifle?.itemMeta
				meta?.lore = listOf("PINK")
				blasterRifle?.itemMeta = meta
				skeleton.equipment.setItemInMainHand(blasterRifle)
			}
		}
	}

	@EventHandler
	fun onEntityDamage(event: EntityDamageByEntityEvent) {
		val entity = event.entity
		val damager = event.damager
		if (entity is Monster && (damager is Monster || damager is Projectile && damager.shooter is Monster)) {
			event.isCancelled = true
		}
	}
}
