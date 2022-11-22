package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.Projectile
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

abstract class multiShotBlaster : Blaster() {
	abstract val multiShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.MultiShotWeaponBalancing

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		ProjectileManager.addProjectile(
			Projectile(
				source.eyeLocation,
				if (getParticleType(source) == Particle.REDSTONE){Particle.DustOptions(getParticleColour(source), multiShotWeaponBalancing.shotSize)} else null,
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