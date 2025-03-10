package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LaserCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

object LaserCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<LaserCannonWeaponSubsystem>(), DisplayNameMultilblock {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): LaserCannonWeaponSubsystem {
		return LaserCannonWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Laser Cannon")
	override val description: Component
		get() = text("A low-power starship weapon.")

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).sponge()
		at(+0, +0, +1).pistonBase()
	}
}
