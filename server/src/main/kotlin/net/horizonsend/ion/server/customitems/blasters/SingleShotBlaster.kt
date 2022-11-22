package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.RayTracedParticleProjectile
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

abstract class SingleShotBlaster : Blaster() {
	abstract val singleShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		ProjectileManager.addProjectile(
			RayTracedParticleProjectile(
				source.eyeLocation,
				singleShotWeaponBalancing.iterationsPerTick,
				singleShotWeaponBalancing.distancePerIteration,
				source,
				singleShotWeaponBalancing.damage,
				singleShotWeaponBalancing.shouldPassThroughEntities,
				singleShotWeaponBalancing.shotSize.toDouble(),
				singleShotWeaponBalancing.shouldBypassHitTicks,
				getParticleType(source),
				if (getParticleType(source) == Particle.REDSTONE){Particle.DustOptions(getParticleColour(source), singleShotWeaponBalancing.shotSize)} else null,
			)
		)
	}
}