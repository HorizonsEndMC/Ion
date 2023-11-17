package net.horizonsend.ion.server.features.customitems.throwables.objects

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.Throwables.ThrowableBalancing
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.function.Supplier

abstract class ThrownCustomItem(
	val item: Item,
	val maxTicks: Int,
	val damageSource: Entity?,
	private val balancingSupplier: Supplier<ThrowableBalancing>
) : BukkitRunnable() {
	val balancing get() = balancingSupplier.get()

	protected var hasImpacted = false
	var ticks: Int = 0

	val world = item.world

	val location get() = item.location

	override fun run() {
		if (item.isDead) return cancel()

		if (!hasImpacted) {
			val block = location.add(item.velocity).block
			val type = block.type

			if (type != Material.AIR && !block.isLiquid) {
				hasImpacted = true
				onImpact(block)
			}
		}

		if (ticks >= maxTicks) {
			onExplode()
			return
		}

		ticks++

		tick()
	}

	override fun cancel() {
		item.remove()
		onDie()

		super.cancel()
	}

	/** Logic for ticking, interval supplied by balancing */
	open fun tick() {}

	/** Logic for when the item is destroyed, or the detonator is cancelled */
	open fun onDie() {}

	/** Logic for when it reaches its max ticks */
	open fun onExplode() {
		item.setGravity(false)
		item.velocity = Vector()
	}

	/** Logic for when it impacts a surface */
	open fun onImpact(hitBlock: Block) {
		item.setGravity(false)
		item.velocity = Vector(0, 0, 0)
	}
}
