package net.horizonsend.ion.server.features.multiblock.starshipweapon

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.kyori.adventure.text.Component
import org.bukkit.block.Sign

// TODO: Make signless multiblocks an actualStyle thing
abstract class SignlessStarshipWeaponMultiblock<TSubsystem : WeaponSubsystem> : Multiblock(), SubsystemMultiblock<TSubsystem> {
	override val signText = arrayOf<Component?>(null, null, null, null)

	override val name: String = javaClass.simpleName

	override fun matchesSign(lines: Array<Component>): Boolean {
		return false
	}

	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return false
	}
}
