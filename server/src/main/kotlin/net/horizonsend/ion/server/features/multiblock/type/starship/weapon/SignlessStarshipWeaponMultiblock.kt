package net.horizonsend.ion.server.features.multiblock.type.starship.weapon

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.SubsystemMultiblock
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.kyori.adventure.text.Component
import org.bukkit.block.Sign

// TODO: Make signless multiblocks an actualStyle thing
abstract class SignlessStarshipWeaponMultiblock<TSubsystem : WeaponSubsystem> : Multiblock(), SubsystemMultiblock<TSubsystem> {
	override val signText = arrayOf<Component?>(null, null, null, null)

	override val name: String = javaClass.simpleName

	override fun matchesSign(lines: List<Component>): Boolean {
		return false
	}

	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return false
	}
}
