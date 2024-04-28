package net.horizonsend.ion.server.features.customitems.blasters.objects


import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Balancing
import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source.PLAYER
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.SoundCategory.PLAYERS
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

abstract class CratePlacer(
	identifier: String,

	material: Material,
	customModelData: Int,
	displayName: Component,
	val magazineType: Magazine<*>,
	val soundReloadStart: String,
	val soundReloadFinish: String,
) : AmmunitionHoldingItem(identifier, material, customModelData, displayName) {
	companion object {
		val magazineSize = 100
		val ammoPerRefill = 1
		val cooldown = 20
	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		placeCrate(livingEntity, itemStack)
	}

	override fun handleTertiaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		//copy-paste from Blaster with some changes
		if (livingEntity !is Player) return // Player Only
		if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown

		val originalAmmo = getAmmunition(itemStack)

		var ammo = originalAmmo

		if (ammo == ((itemStack.customItem as? Blaster<*>)?.getMaximumAmmunition() ?: return)) return

		for (magazineItem in livingEntity.inventory) {
			if (magazineItem == null) continue // check not null
			val magazineCustomItem: CustomItem = magazineItem.customItem ?: continue // To get magazine properties
			if (ammo >= magazineSize) continue // Check if magazine is full
			if (magazineCustomItem.identifier != magazineType.identifier) continue // Only correct magazine

			val magazineAmmo = (magazineCustomItem as AmmunitionHoldingItem).getAmmunition(magazineItem)
			val amountToTake = (magazineSize - ammo).coerceAtMost(magazineAmmo)
			magazineCustomItem.setAmmunition(magazineItem, livingEntity.inventory, magazineAmmo - amountToTake)

			ammo += amountToTake
		}

		if (ammo - originalAmmo == 0) {
			livingEntity.playSound(
				sound(
					key("minecraft:item.bundle.drop_contents"),
					PLAYER,
					5f,
					2.00f
				)
			)
			livingEntity.alert("Out of adhesive!")
			return
		}

		livingEntity.setCooldown(itemStack.type, cooldown)

		setAmmunition(itemStack, livingEntity.inventory, ammo)

		// Finish reload
		Tasks.syncDelay(cooldown.toLong()) {
			livingEntity.location.world.playSound(
				livingEntity.location,
				soundReloadFinish,
				PLAYERS,
				1.0f,
				1.0f
			)
		}

		livingEntity.sendActionBar(text("Ammo: $ammo / ${magazineSize}", NamedTextColor.RED))

		// Start reload
		livingEntity.location.world.playSound(
			livingEntity.location,
			soundReloadStart,
			PLAYERS,
			1.0f,
			1.0f
		)
	}

	override fun getMaximumAmmunition(): Int = magazineSize
	override fun getTypeRefill(): String = magazineType.getTypeRefill()
	override fun getAmmoPerRefill(): Int = ammoPerRefill
	override fun getConsumesAmmo(): Boolean = true

	fun placeCrate(livingEntity: LivingEntity, itemStack: ItemStack) {
		println("boop")
		if (livingEntity !is Player) return
		if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown
		if (getAmmunition(itemStack) == 0) {
			handleTertiaryInteract(livingEntity, itemStack) // Force a reload
			return // No Ammo
		}
		println("boop1")
		//todo fix check currently returing false
		//if (!livingEntity.inventory.containsAtLeast(ItemStack(Material.SHULKER_BOX), 1)) return
		//println("boop2")
		val target = livingEntity.getTargetBlockExact(4) ?: return
		println("target")
		println(target)
		println()
		if (target.type != Material.STICKY_PISTON) return

		val face = livingEntity.getTargetBlockFace(4) ?: return
		//cordinates of the block above where to place crate
		val x = target.x + face.modX
		val y = target.y + face.modY
		val z = target.z + face.modZ

		var toReplace = getBlockIfLoaded(livingEntity.world, x, y, z)!!
		println("to replace")
		println(toReplace)
		println()
		val tempData = toReplace.blockData.clone()
		println("tempData")
		println(tempData)
		println()
		val state = toReplace.state //current listeners dont seem to use this... hopefully
		println("state")
		println(state)
		println()


		for (item in livingEntity.inventory) {
			if (item == null) continue
			if (!item.type.name.contains("shulker_box", ignoreCase = true)) continue
			println("item")
			println(item)
			println()
			//attempt to place the crate
			//fake block place event
			toReplace.setBlockData(item.type.createBlockData(), true)
			//todo add nbtdata
			println("to replace")
			println(toReplace)
			println()
			println("tempData")
			println(tempData)
			println()
			println("state")
			println(state)
			println()
			//event check
			var event = BlockPlaceEvent(toReplace,
										state,
										target,
										item,
										livingEntity,
								true, EquipmentSlot.HAND)
			if (event.callEvent()) {
				println("succes")
				//placement is valid, delete item from inventory and decriement ammo
				livingEntity.inventory.remove(item)
				setAmmunition(itemStack, livingEntity.inventory,getAmmunition(itemStack) - 1)
				break
			} else {
				println("fail")
				//placement is invalid, revert back to old state
				toReplace.setBlockData(tempData, true)
				break
			}
		}

	}

}
