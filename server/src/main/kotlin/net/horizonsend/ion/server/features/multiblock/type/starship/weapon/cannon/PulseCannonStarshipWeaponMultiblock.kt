package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PulseCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

object PulseCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PulseCannonWeaponSubsystem>(), DisplayNameMultilblock {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): PulseCannonWeaponSubsystem {
		return PulseCannonWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Pulse Cannon")
	override val description: Component
		get() = text("A weapon designed to destroy small starships. Only mountable on Gunship and Frigate.")

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).sponge()
		at(+0, +0, +1).anyGlass()
	}
}
