package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TorpedoWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

object TorpedoStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<TorpedoWeaponSubsystem>(), DisplayNameMultilblock {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TorpedoWeaponSubsystem {
		return TorpedoWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Torpedo")
	override val description: Component
		get() = text("A heavy weapon with a homing projectile.")

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).sponge()
		at(+0, +0, +1).sponge()
		at(+0, +0, +2).dispenser()
	}
}
