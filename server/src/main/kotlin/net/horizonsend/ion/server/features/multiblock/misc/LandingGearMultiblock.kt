package net.horizonsend.ion.server.features.multiblock.misc

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SubsystemMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.LandingGearSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

object LandingGearMultiblock : Multiblock(), SubsystemMultiblock<LandingGearSubsystem> {
	override val signText = arrayOf<Component?>(null, null, null, null)

	override val name: String = javaClass.simpleName

	override fun matchesSign(lines: Array<Component>): Boolean {
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
