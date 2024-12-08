package net.horizonsend.ion.server.features.custom.items.throwables.thrown

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.custom.items.throwables.ThrownCustomItem
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Item

class ThrownSmokeGrenade(
	item: Item,
	maxTicks: Int,
	damageSource: Entity?,
) : ThrownCustomItem(item, maxTicks, damageSource, ConfigurationFiles.pvpBalancing().throwables::smokeGrenade) {
	private var isExploding = false

	override fun onImpact(hitBlock: Block) {
		super.onImpact(hitBlock)

		onExplode()
	}

	override fun onExplode() {
		if (isExploding) return
		isExploding = true

		item.remove()

		world.spawnParticle(
			Particle.CAMPFIRE_SIGNAL_SMOKE,
			location.x,
			location.y + 1.0,
			location.z,
			1000,
			2.0,
			1.0,
			2.0,
			0.0,
		)
	}
}
