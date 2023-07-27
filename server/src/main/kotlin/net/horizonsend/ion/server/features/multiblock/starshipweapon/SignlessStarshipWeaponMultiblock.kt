package net.horizonsend.ion.server.features.multiblock.starshipweapon

import net.kyori.adventure.text.Component
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import org.bukkit.block.Sign

// TODO: Make signless multiblocks an actual thing
abstract class SignlessStarshipWeaponMultiblock<TSubsystem : WeaponSubsystem> : StarshipWeaponMultiblock<TSubsystem>() {
	override val signText = arrayOf<Component?>(null, null, null, null)

	override val name: String = javaClass.simpleName

	override fun matchesSign(lines: Array<Component>): Boolean {
		return false
	}

	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return false
	}
}
