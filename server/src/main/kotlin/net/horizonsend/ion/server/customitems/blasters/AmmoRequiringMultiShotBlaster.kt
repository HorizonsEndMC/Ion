package net.horizonsend.ion.server.customitems.blasters

import kotlin.math.absoluteValue
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.Projectile
import net.starlegacy.util.randomDouble
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

abstract class AmmoRequiringMultiShotBlaster : multiShotBlaster(){
	abstract val requiredAmmo: ItemStack

	override fun onPrimaryInteract(source: LivingEntity, itemStack: ItemStack) {
		if ((source as? Player)?.hasCooldown(itemStack.type) == true) return
		if(reload(itemStack, source)) return
		source.location.world.playSound(source.location,"laser", 1f, multiShotWeaponBalancing.pitch)
		for (i in 1..multiShotWeaponBalancing.shotCount) {
			val offsetX = randomDouble(-1*multiShotWeaponBalancing.offsetmax, multiShotWeaponBalancing.offsetmax)
			val offsetY = randomDouble(-1*multiShotWeaponBalancing.offsetmax, multiShotWeaponBalancing.offsetmax)
			val offsetZ = randomDouble(-1*multiShotWeaponBalancing.offsetmax, multiShotWeaponBalancing.offsetmax)

			val location = source.eyeLocation.clone()

			location.direction = location.direction.add(Vector(offsetX, offsetY, offsetZ))

			location.direction.multiply(Vector(offsetX, offsetY, offsetZ))
			ProjectileManager.addProjectile(
				Projectile(
					location,
					if (getParticleType(source) == Particle.REDSTONE) {
						Particle.DustOptions(getParticleColour(source), multiShotWeaponBalancing.shotSize)
					} else null,
					multiShotWeaponBalancing.iterationsPerTick,
					multiShotWeaponBalancing.distancePerIteration,
					getParticleType(source),
					source,
					multiShotWeaponBalancing.damage,
					multiShotWeaponBalancing.shouldPassThroughEntities,
					multiShotWeaponBalancing.shotSize.toDouble(),
					multiShotWeaponBalancing.shouldBypassHitTicks
				)
			)
		}
	}
	fun reload(itemStack: ItemStack, source: LivingEntity): Boolean {
		if ((source as? Player)!!.hasCooldown(itemStack.type)) return true
		if (itemStack.itemMeta.persistentDataContainer == null){
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
		val pdc = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "blaster"), PersistentDataType.INTEGER)
		if (pdc?.absoluteValue != 0){
			itemStack.editMeta {
				itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "blaster"), PersistentDataType.INTEGER)?.minus(1)
			}
			(source as? Player)?.setCooldown(itemStack.type, this.multiShotWeaponBalancing.timeBetweenShots)
			return false
		}
		if (pdc.absoluteValue.absoluteValue == 0){
			(source as? Player)?.setCooldown(itemStack.type, this.multiShotWeaponBalancing.reload)
			if (!(source as? Player)?.inventory!!.contains(this.requiredAmmo)) return true
			(source as? Player)?.inventory?.remove(this.requiredAmmo)
			return true
		}
		return true
	}
}