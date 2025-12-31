package net.horizonsend.ion.server.features.custom.items.type.weapon.sword

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getEnumSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.miscellaneous.AudioRange
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import kotlin.math.roundToInt

object SwordListener : IonServerComponent() {
	override fun onEnable() {
		// Energy sword idle sound
		// Use async task and while loop with thread sleep so when it lags it doesnt sound weird
		// The timing of the sounds is very important
		Tasks.async {
			while (IonServer.isEnabled) {
				Tasks.sync {
					for (player in Bukkit.getOnlinePlayers()) {
						val main = player.inventory.itemInMainHand
						val offhand = player.inventory.itemInOffHand

						val mainCustomItem = main.customItem
						val offhandCustomItem = offhand.customItem

						if (mainCustomItem != null && mainCustomItem is EnergySword ||
							offhandCustomItem != null && offhandCustomItem is EnergySword ||
							player.getSettingOrThrow(PlayerSettings::energySwordIdleSound)
						) {
							player.world.playSound(player.location, "horizonsend:energy_sword.idle", 5.0f, 1.0f)
						}
					}
				}

				try {
					Thread.sleep(2000)
				} catch (e: InterruptedException) {
					e.printStackTrace()
				}
			}
		}
	}
	@EventHandler
	fun onPlayerItemHoldEvent(event: PlayerItemHeldEvent) {
		val itemStack = event.player.inventory.getItem(event.newSlot) ?: event.player.inventory.itemInOffHand
		val customItem = itemStack.customItem as? EnergySword ?: return

		val block = customItem.blockComponent.getBlock(itemStack, event.player)

		customItem.blockComponent.sendActionBar(block, event.player)
	}

	@Suppress("DEPRECATION") //Deprecate because EntityDamageEvent.DamageModifier is deprecated despite there being no alternative
	@EventHandler(priority = EventPriority.LOW)
	fun onShieldHit(event: EntityDamageByEntityEvent){
		val item = (event.entity as? Player)?.activeItem //Get the item in use
		val customItem = item?.customItem ?: return //Get the customitem in use
		if (customItem !is EnergySword) return //Check if it's an energysword
		//If the player already has a cooldown on the shield then we dont need to do anything, and we can let the event play out normally
		if ((event.entity as? Player)?.hasCooldown(item) == true) return
		if ((event.entity as? Player)?.isBlocking == false) return

		val blockComponent = customItem.blockComponent

		val currentBlock = blockComponent.getBlock(item, event.entity as LivingEntity) //Get current Block
		val damagedBlocked = -event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) //Get the damage that a shield absorbed in the event
		if (currentBlock <= 0.0) {
			//If the current block is broken <=0 then set the cooldown, and importantly clear the active item so the player stops using the shield, to get infinite block
			(event.entity as? Player)?.setCooldown(item.type, (customItem.balancing.blockAmount/customItem.balancing.blockRechargePerTick).toInt())
			(event.entity as LivingEntity).clearActiveItem()
			return
		}

		// If the block isint zero then minus the damageBlocked from the currentBlock
		// 10 is a constant adjustment, 1 damage = 10 block
		blockComponent.setBlock(item, (currentBlock-damagedBlocked*10).roundToInt(), event.entity as LivingEntity)

		//If the currentBlock couldnt absorb all the damage that was absorbed by the shield during the event, damage the player with the excess damage
		val damageThatShouldHaveBeenDealt = damagedBlocked-currentBlock
		if (damageThatShouldHaveBeenDealt < 0.0) return
		//I understand this is deprecated however it seems no alternative exists that achieves the same result
		event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, damagedBlocked-currentBlock)
		if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) <=0.0) event.isCancelled = true
	}

}
