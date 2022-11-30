package net.horizonsend.ion.server.customitems.blasters.constructors

import io.papermc.paper.entity.RelativeTeleportFlag
import net.horizonsend.ion.server.IonServer
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
	abstract val requiredAmmo: ItemStack

	override fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {
		val player = entity as? Player
		if ((entity as? Player)!!.hasCooldown(item.type)) return
		if (reload(item, entity)) return
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
						if (reload(item, entity)) return@syncDelay
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
							reload(itemInOffHand, entity)
						}
					} else if (itemInOffHand.getCustomItem() is MultiShotBlaster) {
						(itemInOffHand.getCustomItem() as MultiShotBlaster).apply {
							if (reload(item, entity)) return@syncDelay
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

	override fun onTertiaryInteract(source: LivingEntity, item: ItemStack) {
		val player = (source as? Player)
		//if the player has already reloaded/is shooting
		if (player?.hasCooldown(item.type) == true) return
		val inventory = (source as? Player)?.inventory
		//this is so if you reload, while your magazine is full, it will stop you
		if (item.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER) == singleShotWeaponBalancing.magazineSize) return
		//make sure they have the correct ammo
		if (!inventory!!.containsAtLeast(requiredAmmo, requiredAmmo.amount)) return
		//remove the ammo
		inventory.removeItemAnySlot(requiredAmmo.clone())
		source.updateInventory()

		item.editMeta {
			it.lore()?.clear()
			it.lore(customItemlist.itemStack.lore())
		}
		//removing the pdc for ammo
		item.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }
		//adding back the pdc for ammo
		item.editMeta {
			it.persistentDataContainer[NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER] =
				singleShotWeaponBalancing.magazineSize
		}
		//add in the reload cooldown
		player?.setCooldown(item.type, this.singleShotWeaponBalancing.reload)
		player?.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Ammo: ${singleShotWeaponBalancing.magazineSize}/${singleShotWeaponBalancing.magazineSize}"))
	}

	fun reload(item: ItemStack, source: LivingEntity): Boolean {
		val pdc = item.itemMeta.persistentDataContainer.get(
			NamespacedKey(IonServer.Ion, "ammo"),
			PersistentDataType.INTEGER
		)

		if (pdc != 0 && pdc != null) {
			val ammoValue: Int = pdc - 1

			item.editMeta {
				it.lore()?.clear()
				it.lore(mutableListOf(MiniMessage.miniMessage()
					.deserialize("<bold><gray>Ammo:${ammoValue}/${singleShotWeaponBalancing.magazineSize}")))
			}

			(source as? Player)?.setCooldown(item.type, singleShotWeaponBalancing.timeBetweenShots)
			item.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }
			item.editMeta {
				it.persistentDataContainer.set(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER,
					ammoValue
				)
			}
			(source as? Player)?.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Ammo: $ammoValue/${singleShotWeaponBalancing.magazineSize}"))

			return false
		}

		return true
	}

	@Suppress("UnstableApiUsage")

	private fun recoil(entity: LivingEntity){
		if (entity is Flying) return
		val recoil = singleShotWeaponBalancing.recoil/singleShotWeaponBalancing.packetsPerShot
		for (i in 1..singleShotWeaponBalancing.packetsPerShot){
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