package net.horizonsend.ion.server.features.customitems.throwables

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.Throwables.ThrowableBalancing
import net.horizonsend.ion.server.features.customitems.throwables.objects.ThrownCustomItem
import net.horizonsend.ion.server.features.starship.damager.addToDamagers
import net.horizonsend.ion.server.features.starship.damager.damager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.regeneratingBlockChange
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import java.util.function.Supplier

class ThrownDetonator(
	item: Item,
	maxTicks: Int,
	damageSource: Entity?,
	balancingSupplier: Supplier<ThrowableBalancing>
) : ThrownCustomItem(item, maxTicks, damageSource, balancingSupplier) {
	var isExploding = false

	override fun onImpact(hitBlock: Block) {
		super.onImpact(hitBlock)

		onExplode()
	}

	override fun onExplode() {
		if (isExploding) return
		isExploding = true

		var explosionTicks = 0
		Tasks.bukkitRunnable {
			if (item.isDead) return@bukkitRunnable cancel()
			if (hasImpacted || ticks >= maxOf(ticks, maxTicks - 10)) world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BELL, 10f, 1.0f)
			explosionTicks++

			if (explosionTicks < 20) return@bukkitRunnable

			doExplosion()
			this.cancel()
		}.runTaskTimer(IonServer, 0L, 1L)
	}

	private fun doExplosion() {
		val location = location
		item.remove()

		val blocks = mutableListOf<Block>()
		val block = item.location.block

		for (x in -2..2) {
			for (y in -2..2) {
				for (z in -2..2) {
					val toExplode = block.getRelative(BlockFace.EAST, x)
						.getRelative(BlockFace.UP, y)
						.getRelative(BlockFace.SOUTH, z)

					if (toExplode.type != Material.AIR) {
						blocks.add(toExplode)
					}
				}
			}
		}

		val event = regeneratingBlockChange(item, block, blocks, 0.123f, true)
		val called = event.callEvent()

		if (!called && !world.name.contains("arena", ignoreCase = true)) return

		blocks.forEach { it.setType(Material.AIR, false) }

		damageSource?.damager()?.let {
			addToDamagers(
				world,
				block,
				it
			)
		}

		world.getNearbyEntities(location, balancing.damageRadius, balancing.damageRadius, balancing.damageRadius)
			.map { it as? Damageable }
			.forEach { it?.damage(balancing.damage / it.location.distance(location), damageSource) }
	}
}
