package net.horizonsend.ion.server.customitems.blasters

import kotlin.math.absoluteValue
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.RayTracedParticleProjectile
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
					multiShotWeaponBalancing.iterationsPerTick,
					multiShotWeaponBalancing.distancePerIteration,
					entity,
					multiShotWeaponBalancing.damage,
					multiShotWeaponBalancing.shouldPassThroughEntities,
					multiShotWeaponBalancing.shotSize.toDouble(),
					multiShotWeaponBalancing.shouldBypassHitTicks,
					getParticleType(entity),
					if (getParticleType(entity) == Particle.REDSTONE) {
						Particle.DustOptions(getParticleColour(entity), multiShotWeaponBalancing.shotSize)
					} else null,
				)
			)
		}
	}

	fun reload(itemStack: ItemStack, source: LivingEntity): Boolean {
		if ((source as? Player)!!.hasCooldown(itemStack.type)) return true

		if (itemStack.itemMeta.persistentDataContainer == null) {
			itemStack.editMeta {
				it.persistentDataContainer.set(
					NamespacedKey(IonServer.Ion, "blaster"),
					PersistentDataType.INTEGER,
					this.multiShotWeaponBalancing.magazineSize.minus(1)
				)
			}

			(source as? Player)?.setCooldown(itemStack.type, this.multiShotWeaponBalancing.timeBetweenShots)
			return false
		}

		val pdc = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "blaster"),
			PersistentDataType.INTEGER)

		if (pdc?.absoluteValue != 0) {
			itemStack.editMeta {
				itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "blaster"),
					PersistentDataType.INTEGER)?.minus(1)
			}

			(source as? Player)?.setCooldown(itemStack.type, this.multiShotWeaponBalancing.timeBetweenShots)
			return false
		}

		if (pdc.absoluteValue.absoluteValue == 0) {
			(source as? Player)?.setCooldown(itemStack.type, this.multiShotWeaponBalancing.reload)

			if (!(source as? Player)?.inventory!!.contains(this.requiredAmmo)) return true
			(source as? Player)?.inventory?.remove(this.requiredAmmo)
			return true
		}

		return true
	}
}