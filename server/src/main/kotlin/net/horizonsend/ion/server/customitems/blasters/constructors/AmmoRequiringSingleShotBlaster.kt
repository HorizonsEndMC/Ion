package net.horizonsend.ion.server.customitems.blasters.constructors

import io.papermc.paper.entity.RelativeTeleportFlag
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.blasters.StandardMagazine.getAmmo
import net.horizonsend.ion.server.customitems.blasters.StandardMagazine.setAmmo
import net.horizonsend.ion.server.customitems.getCustomItem
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.RayTracedParticleProjectile
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.util.Tasks
import net.starlegacy.util.randomDouble
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Flying
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

abstract class AmmoRequiringSingleShotBlaster : SingleShotBlaster() {
	abstract val requiredAmmo: Magazine

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		return
	}

	// Fires the gun.
	override fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {
		val player = entity as? Player

		if (player?.hasCooldown(item.type) == true) return

		if (canFire(item, entity)) return

		entity.location.world.playSound(entity.location, "laser", 1f, singleShotWeaponBalancing.pitch)
		ProjectileManager.addProjectile(
			RayTracedParticleProjectile(
				entity.eyeLocation,
				singleShotWeaponBalancing.speed,
				entity,
				singleShotWeaponBalancing.damage,
				singleShotWeaponBalancing.damageFalloffMultiplier,
				singleShotWeaponBalancing.shouldPassThroughEntities,
				singleShotWeaponBalancing.shotSize.toDouble(),
				singleShotWeaponBalancing.shouldBypassHitTicks,
				singleShotWeaponBalancing.range,
				getParticleType(entity),
				if (getParticleType(entity) == Particle.REDSTONE) {
					Particle.DustOptions(getParticleColour(entity), 1f)
				} else null
			)
		)
		recoil(entity)
		/**
		 * I thought it'd be funny to add in akimbo for pistols
		 */
		val itemInOffHand = player?.inventory?.itemInOffHand
		//customId for the offhand item
		val offHandPDC = itemInOffHand?.itemMeta?.persistentDataContainer?.get(
			NamespacedKey(IonServer.Ion, "CustomID"),
			PersistentDataType.STRING
		)
		//customId for the item in main hand
		val itemPDC = item.itemMeta.persistentDataContainer.get(
			NamespacedKey(IonServer.Ion, "CustomID"),
			PersistentDataType.STRING
		)
		//if the item in offhand is a custom item
		Tasks.syncDelay((singleShotWeaponBalancing.timeBetweenShots / 2).toLong()) {
			if (itemInOffHand?.getCustomItem() != null) {
				//if the weapon does not want akimbo, set the cooldown for that item
				if (!singleShotWeaponBalancing.shouldAkimbo) {
					player.setCooldown(itemInOffHand.type, singleShotWeaponBalancing.timeBetweenShots)
				}
				/**
				 * if akimbo is allowed and the two customId's are equal, make another particle
				 */
				else if (singleShotWeaponBalancing.shouldAkimbo && itemPDC == offHandPDC) {
					if (itemInOffHand.getCustomItem() is SingleShotBlaster) {
						if (canFire(item, entity)) return@syncDelay
						(itemInOffHand.getCustomItem() as SingleShotBlaster).apply {
							ProjectileManager.addProjectile(
								RayTracedParticleProjectile(
									entity.eyeLocation,
									singleShotWeaponBalancing.speed,
									entity,
									singleShotWeaponBalancing.damage,
									singleShotWeaponBalancing.damageFalloffMultiplier,
									singleShotWeaponBalancing.shouldPassThroughEntities,
									singleShotWeaponBalancing.shotSize.toDouble(),
									singleShotWeaponBalancing.shouldBypassHitTicks,
									singleShotWeaponBalancing.range,
									getParticleType(entity),
									if (getParticleType(entity) == Particle.REDSTONE) {
										Particle.DustOptions(getParticleColour(entity), 1f)
									} else null
								)
							)
							canFire(itemInOffHand, entity)
						}
					} else if (itemInOffHand.getCustomItem() is MultiShotBlaster) {
						(itemInOffHand.getCustomItem() as MultiShotBlaster).apply {
							if (canFire(item, entity)) return@syncDelay
							for (i in 1..multiShotWeaponBalancing.shotCount) {
								val offsetX = randomDouble(
									-1 * multiShotWeaponBalancing.offsetMax,
									multiShotWeaponBalancing.offsetMax
								)
								val offsetY = randomDouble(
									-1 * multiShotWeaponBalancing.offsetMax,
									multiShotWeaponBalancing.offsetMax
								)
								val offsetZ = randomDouble(
									-1 * multiShotWeaponBalancing.offsetMax,
									multiShotWeaponBalancing.offsetMax
								)

								val location = entity.eyeLocation.clone()

								location.direction = location.direction.normalize()

								location.direction = location.direction.add(Vector(offsetX, offsetY, offsetZ))

								ProjectileManager.addProjectile(
									RayTracedParticleProjectile(
										location,
										multiShotWeaponBalancing.speed,
										entity,
										multiShotWeaponBalancing.damage,
										multiShotWeaponBalancing.damageFalloffMultiplier,
										multiShotWeaponBalancing.shouldPassThroughEntities,
										multiShotWeaponBalancing.shotSize.toDouble(),
										multiShotWeaponBalancing.shouldBypassHitTicks,
										multiShotWeaponBalancing.range,
										getParticleType(entity),
										if (getParticleType(entity) == Particle.REDSTONE) {
											Particle.DustOptions(getParticleColour(entity), 1f)
										} else null,
									)
								)
							}
						}
					}
				}
			}
		}
	}

	override fun onTertiaryInteract(entity: LivingEntity, item: ItemStack) {
		val player = (entity as? Player) ?: return // Mobs don't need to reload

		if (player.hasCooldown(item.type)) return // Can't reload while on cooldown

		val ammoCount = item.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "ammo"),
			PersistentDataType.INTEGER) ?: return // Fail if PDC is broken

		if (ammoCount == singleShotWeaponBalancing.magazineSize) return // Cancels reload if the magazine is full.

		val magazine = player.inventory.find { inventoryItem -> inventoryItem.getCustomItem() is Magazine } ?: return // Return if no mags

		val magAmmo = getAmmo(magazine) ?: return // Shouldn't be null, will return if the PDC was broken.

		// Calculate the maximum the gun can be filled to from the first available magazine.
		val maxFilled: Int = if (magAmmo > singleShotWeaponBalancing.magazineSize - ammoCount) {
			// If the new magazine capacity is above zero, set the magazine capacity to the new amount, and fill the gun to its full capacity
			setAmmo(magazine, player, magAmmo - (singleShotWeaponBalancing.magazineSize - ammoCount))

			singleShotWeaponBalancing.magazineSize
		} else {
			// If the new magazine capacity is below zero, fill the gun to what was left, and delete the magazine
			player.inventory.removeItemAnySlot(magazine.clone())
			player.updateInventory()

			magAmmo + ammoCount
		}

		item.editMeta {
			it.lore()?.clear()
			it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: $maxFilled/${singleShotWeaponBalancing.magazineSize}")))
		}

		player.setCooldown(item.type, this.singleShotWeaponBalancing.reload)

		item.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }

		item.editMeta {
			it.persistentDataContainer.set(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER,
				maxFilled
			)
		}

		player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Ammo: $maxFilled/${singleShotWeaponBalancing.magazineSize}"))
	}

	fun canFire(item: ItemStack, source: LivingEntity): Boolean {
		val pdc = item.itemMeta.persistentDataContainer.get(
			NamespacedKey(IonServer.Ion, "ammo"),
			PersistentDataType.INTEGER
		)

		if (pdc != 0 && pdc != null) { // Checks if the blaster has no ammo
			val ammoValue: Int = pdc - 1 // If it has no ammo, lower the ammo count by one

			item.editMeta {// Reflect the new ammo count in the item lore
				it.lore()?.clear()
				it.lore(mutableListOf(
					MiniMessage.miniMessage()
						.deserialize("<bold><gray>Ammo:${ammoValue}/${singleShotWeaponBalancing.magazineSize}")))
			}

			item.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) } // Delete the old ammo count

			item.editMeta {
				it.persistentDataContainer.set(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER,
					ammoValue
				) // Reflect the new ammo count in the item PDC
			}

			source.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Ammo: $ammoValue/${singleShotWeaponBalancing.magazineSize}"))

			(source as? Player)?.setCooldown(item.type, singleShotWeaponBalancing.timeBetweenShots) // Set the cooldown for the next shot

			return false
		}

		return true
	}

	@Suppress("UnstableApiUsage")
	private fun recoil(entity: LivingEntity) {
		val recoil = singleShotWeaponBalancing.recoil / singleShotWeaponBalancing.packetsPerShot

		for (i in 1..singleShotWeaponBalancing.packetsPerShot) {
			if (entity is Flying) return

			Tasks.syncDelay(i.toLong()) {
				val loc = entity.location

				loc.pitch -= recoil

				(entity as? Player)?.teleport(
					loc,
					PlayerTeleportEvent.TeleportCause.PLUGIN,
					true,
					false,
					*RelativeTeleportFlag.values()
				)
			}
		}
	}
}