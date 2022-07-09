package net.horizonsend.ion.server.customitems

import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeaponBalancing.WeaponBalancing
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.Projectile
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

abstract class SingleShotBlaster : Blaster() {
	protected abstract val weaponBalancing: WeaponBalancing

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		ProjectileManager.addProjectile(
			Projectile(
				source.eyeLocation,
				Particle.DustOptions(getCosmeticColor(source), weaponBalancing.shotSize),
				weaponBalancing.iterationsPerTick,
				weaponBalancing.distancePerIteration
			)
		)
	}
}