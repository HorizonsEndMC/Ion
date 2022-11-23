package net.horizonsend.ion.server.customitems.blasters.constructors

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.RayTracedParticleProjectile
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.util.randomDouble
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

abstract class AmmoRequiringMultiShotBlaster : MultiShotBlaster() {
	abstract val requiredAmmo: ItemStack

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		if (!(source as? Player)?.inventory!!.contains(requiredAmmo)) return
		(source as? Player)?.inventory
		item.editMeta {
			it.lore()?.clear()
			it.lore(customItemlist.itemStack.lore())
		}
		item.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }
		item.editMeta {
			it.persistentDataContainer[NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER] =
				multiShotWeaponBalancing.magazineSize
		}
		(source as? Player)?.setCooldown(item.type, this.multiShotWeaponBalancing.reload)
	}
	override fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {
		if ((entity as? Player)?.hasCooldown(item.type) == true) return

		if (reload(item, entity)) return

		entity.location.world.playSound(entity.location, "laser", 1f, multiShotWeaponBalancing.pitch)

		for (i in 1..multiShotWeaponBalancing.shotCount) {
			val offsetX = randomDouble(-1 * multiShotWeaponBalancing.offsetMax, multiShotWeaponBalancing.offsetMax)
			val offsetY = randomDouble(-1 * multiShotWeaponBalancing.offsetMax, multiShotWeaponBalancing.offsetMax)
			val offsetZ = randomDouble(-1 * multiShotWeaponBalancing.offsetMax, multiShotWeaponBalancing.offsetMax)

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
						Particle.DustOptions(getParticleColour(entity), multiShotWeaponBalancing.shotSize)
					} else null,
				)
			)
		}
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
				it.lore(mutableListOf(
					MiniMessage.miniMessage()
					.deserialize("<bold><gray>Ammo:${ammoValue}/${multiShotWeaponBalancing.magazineSize}")))
			}

			(source as? Player)?.setCooldown(item.type, multiShotWeaponBalancing.timeBetweenShots)
			item.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }
			item.editMeta {
				it.persistentDataContainer.set(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER,
					ammoValue
				)
			}

			return false
		}

		return true
	}
}