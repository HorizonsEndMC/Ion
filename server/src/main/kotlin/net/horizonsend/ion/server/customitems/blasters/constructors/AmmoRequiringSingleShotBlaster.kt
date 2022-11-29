package net.horizonsend.ion.server.customitems.blasters.constructors

import io.papermc.paper.entity.RelativeTeleportFlag
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.RayTracedParticleProjectile
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.util.Tasks
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Flying
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class AmmoRequiringSingleShotBlaster : SingleShotBlaster() {
	abstract val requiredAmmo: ItemStack

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		val player = (source as? Player)
		if (player?.hasCooldown(item.type) == true) return
		val inventory = (source as? Player)?.inventory
		if (!inventory!!.containsAtLeast(requiredAmmo, 1)) return
		inventory.removeItemAnySlot(requiredAmmo.clone())
		source.updateInventory()

		item.editMeta {
			it.lore()?.clear()
			it.lore(customItemlist.itemStack.lore())
		}
		item.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }
		item.editMeta {
			it.persistentDataContainer[NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER] =
				singleShotWeaponBalancing.magazineSize
		}
		player?.setCooldown(item.type, this.singleShotWeaponBalancing.reload)
		player?.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Ammo: ${singleShotWeaponBalancing.magazineSize}/${singleShotWeaponBalancing.magazineSize}"))
	}

	override fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {
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
					Particle.DustOptions(getParticleColour(entity), singleShotWeaponBalancing.shotSize)
				} else null
			)
		)
		recoil(entity)
	}

	fun reload(item: ItemStack, source: LivingEntity): Boolean {
		if ((source as? Player)!!.hasCooldown(item.type)) return true

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