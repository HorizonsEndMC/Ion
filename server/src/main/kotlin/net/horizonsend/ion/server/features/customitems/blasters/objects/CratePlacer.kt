package net.horizonsend.ion.server.features.customitems.blasters.objects


import fr.skytasul.guardianbeam.Laser.GuardianLaser
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentManager.getShipmentItemId
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source.PLAYER
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.BlockPos
import net.minecraft.nbt.StringTag
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory.PLAYERS
import org.bukkit.block.ShulkerBox
import org.bukkit.craftbukkit.v1_20_R3.block.CraftShulkerBox
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

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
		val range = 16;
	}
	override val displayAmmo = true

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		if (livingEntity !is Player) return
		if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown
		if (getAmmunition(itemStack) == 0) {
			handleTertiaryInteract(livingEntity, itemStack) // Force a reload
			return // No Ammo
		}
		placeCrate(livingEntity, itemStack)
		fireLaser(livingEntity)
	}

	override fun handleTertiaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		//copy-paste from Blaster with some changes
		if (livingEntity !is Player) return // Player Only
		if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown
		val originalAmmo = getAmmunition(itemStack)

		var ammo = originalAmmo

		if (ammo == ((itemStack.customItem as? CratePlacer)?.getMaximumAmmunition() ?: return)) return

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

		livingEntity.sendActionBar(text("Ammo: $ammo / ${magazineSize}", NamedTextColor.GOLD))

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

	private fun placeCrate(livingEntity: Player, itemStack: ItemStack) {
		//todo fix check currently returing false
		//if (!livingEntity.inventory.containsAtLeast(ItemStack(Material.SHULKER_BOX), 1)) return
		val target = livingEntity.getTargetBlockExact(range) ?: return
		if (target.type != Material.STICKY_PISTON) return

		val face = livingEntity.getTargetBlockFace(range) ?: return
		//cordinates of the block above where to place crate
		val x = target.x + face.modX
		val y = target.y + face.modY
		val z = target.z + face.modZ

		val toReplace = livingEntity.world.getBlockAt(x, y, z)
		// if you have lava on your ship I will judge you
		if (!(toReplace.type == Material.AIR || toReplace.type == Material.WATER) ) return
		val tempData = toReplace.blockData.clone()
		val state = toReplace.state //current listeners dont seem to use this... hopefully


		for (item in livingEntity.inventory) {
			if (item == null) continue
			if (!item.type.name.contains("shulker_box", ignoreCase = true)) continue
			val itemState = (item.itemMeta as BlockStateMeta).blockState as ShulkerBox
			//attempt to place the crate
			//I copied gutins code and prayed that it worked
			//fake block place event
			toReplace.setBlockData(item.type.createBlockData(), true)

			val boxEntity = toReplace.state as ShulkerBox
			boxEntity.customName = item.itemMeta.displayName
			boxEntity.inventory.addItem(*itemState.inventory.filterNotNull().toList().toTypedArray())
			boxEntity.update()

			// Add the raw nms tag for shipment id
			val id = getShipmentItemId(item)
			val entity = (toReplace.state as CraftShulkerBox).tileEntity
			val chunk = entity.location.chunk.minecraft
			// Save the full compound tag
			val base = entity.saveWithFullMetadata()
			//incomplete crates dont have shipment ids
			if (id != null) base.put("shipment_oid", StringTag.valueOf(id))

			val blockPos = BlockPos(x, y, z)
			// Remove old
			chunk.removeBlockEntity(blockPos)

			val blockEntity = BlockEntity.loadStatic(blockPos, entity.blockState, base)!!
			chunk.addAndRegisterBlockEntity(blockEntity)
			//event check
			val event = BlockPlaceEvent(toReplace,
										state,
										target,
										item,
										livingEntity,
								true, EquipmentSlot.HAND)
			if (event.callEvent()) {
				//placement is valid, delete item from inventory and decriement ammo
				livingEntity.inventory.removeItem(item.asOne())
				setAmmunition(itemStack, livingEntity.inventory,getAmmunition(itemStack) - 1)
				toReplace.world.playSound(
					toReplace.location,
					"minecraft:block.stone.place",
					PLAYERS,
					1.0f,
					1.0f)
				toReplace.world.playSound(
					toReplace.location,
					"minecraft:block.honey_block.slide",
					PLAYERS,
					1.0f,
					1.0f)
				break
			} else {
				//placement is invalid, revert back to old state
				toReplace.setBlockData(tempData, true)
				break
			}
		}

	}

	private fun fireLaser(livingEntity: LivingEntity) {
		val start = livingEntity.eyeLocation.clone()
		start.y -= 0.15

		val raytrace = livingEntity.world.rayTrace(
			livingEntity.eyeLocation,
			start.direction.clone(),
			range.toDouble(),
			FluidCollisionMode.NEVER,
			true,
			0.1,
			{false}
		)
		val end : Location = raytrace?.hitPosition?.toLocation(livingEntity.world)
			?: livingEntity.eyeLocation.clone().add(start.direction.clone().multiply(range))
		GuardianLaser(end,start,5, -1).durationInTicks().start(IonServer)
		start.world.playSound(
			start,
			"minecraft:entity.guardian.attack",
			PLAYERS,
			1.0f,
			1.0f)
	}

}
