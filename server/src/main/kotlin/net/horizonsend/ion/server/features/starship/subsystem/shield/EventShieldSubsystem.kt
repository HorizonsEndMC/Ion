package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.server.features.multiblock.type.particleshield.EventShieldMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Block
import org.bukkit.block.Sign

class EventShieldSubsystem(starship: ActiveStarship, sign: Sign) : ShieldSubsystem(starship, sign, EventShieldMultiblock) {
	override val maxPower: Int
		get() = super.maxPower * 10

	override var power: Int = maxPower
		set(value) {
			field = value.coerceIn(0, maxPower)
		}

	override fun containsBlock(block: Block): Boolean {
		return block.world.uid == starship.world.uid && starship.contains(block.x, block.y, block.z)
	}
}
