package net.horizonsend.ion.server.features.multiblock.type.starship

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.misc.LandingGearSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

object LandingGearMultiblock : Multiblock(), SubsystemMultiblock<LandingGearSubsystem>, DisplayNameMultilblock {
	override val displayName: Component get() = text("Landing Gear")
	override val description: Component get() = text("Automatically extends and retracts when a starship is turned on or off.")

	override val signText = arrayOf<Component?>(null, null, null, null)

	override val name: String = javaClass.simpleName

	override fun matchesSign(lines: List<Component>): Boolean {
		return false
	}

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).type(Material.OBSERVER)
		at(0, -1, 0).pistonBase()
	}

	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return false
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): LandingGearSubsystem {
		return LandingGearSubsystem(starship, pos)
	}
}
