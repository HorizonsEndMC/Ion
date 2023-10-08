package net.horizonsend.ion.server.features.customitems.throwables

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.BalancingConfiguration
import net.horizonsend.ion.server.features.customitems.throwables.objects.ThrownCustomItem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import java.util.function.Supplier

class ThrownIncendiaryGrenade(
	item: Item,
	maxTicks: Int,
	damageSource: Entity?,
	balancingSupplier: Supplier<BalancingConfiguration.Throwables.ThrowableBalancing>
) : ThrownCustomItem(item, maxTicks, damageSource, balancingSupplier) {
	private var isExploding = false

	override fun tick() {
		world.spawnParticle(Particle.SMALL_FLAME, location, 1, 0.0, 0.0, 0.0, 0.0, null)
		world.spawnParticle(Particle.SMOKE_NORMAL, location, 1, 0.0, 0.0, 0.0, 0.0, null)
	}

	override fun onImpact(hitBlock: Block) {
		super.onImpact(hitBlock)

		onExplode()
	}

	override fun onExplode() {
		if (isExploding) return
		isExploding = true

		item.remove()

		var fireLife = 0

		val (originX, originY, originZ) = Vec3i(location)

		Tasks.bukkitRunnable {
			fireLife++

			if (fireLife >= 200) return@bukkitRunnable cancel()

			val radius = if (fireLife > 100) {
				balancing.damageRadius * (((fireLife - 100.0) / -100.0) + 1)
			} else balancing.damageRadius

			world.spawnParticle(Particle.FLAME, location, 75, radius, 0.5, radius, 0.0, null)
			world.spawnParticle(Particle.LAVA, location, 5, radius, 0.5, radius, 0.0, null)
			world.spawnParticle(Particle.SMOKE_LARGE, location, 5, radius, 0.5, radius, 0.0, null)

			world.getNearbyEntities(location, radius, 0.5, radius)
				.map { it as? Damageable }
				.forEach {
					it?.damage(balancing.damage / it.location.distance(location), damageSource)
					it?.fireTicks?.inc()
				}
		}.runTaskTimer(IonServer, 0L, 1L)
	}
}
