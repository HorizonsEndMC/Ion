package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.Projectile
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

abstract class SingleShotBlaster : Blaster() {
	abstract val singleShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		ProjectileManager.addProjectile(
			Projectile(
				source.eyeLocation,
				if (getParticleType(source) == Particle.REDSTONE){Particle.DustOptions(getParticleColour(source), singleShotWeaponBalancing.shotSize)} else null,
				singleShotWeaponBalancing.iterationsPerTick,
				singleShotWeaponBalancing.distancePerIteration,
				getParticleType(source),
				source,
				singleShotWeaponBalancing.damage,
				singleShotWeaponBalancing.shouldPassThroughEntities,
				singleShotWeaponBalancing.shotSize.toDouble()
			)
		)
	}
}