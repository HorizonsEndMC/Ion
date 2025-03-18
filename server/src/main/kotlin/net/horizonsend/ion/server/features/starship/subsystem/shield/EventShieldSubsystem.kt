package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.server.features.multiblock.type.particleshield.EventShieldMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World
import org.bukkit.block.Sign

class EventShieldSubsystem(starship: ActiveStarship, sign: Sign) : ShieldSubsystem(starship, sign, EventShieldMultiblock) {
	override val maxPower: Int
		get() = super.maxPower * 5

	override var power: Int = maxPower
		set(value) {
			if (value > field) return // prevent regen
			field = value.coerceIn(0, maxPower)
		}

	override fun containsPosition(world: World, blockPos: Vec3i): Boolean {
		return world.uid == starship.world.uid && starship.contains(blockPos.x, blockPos.y, blockPos.z)
	}
}
