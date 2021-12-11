package net.starlegacy.feature.multiblock.starshipweapon

import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import org.bukkit.block.Sign

// TODO: Make signless multiblocks an actual thing
abstract class SignlessStarshipWeaponMultiblock<TSubsystem : WeaponSubsystem> : StarshipWeaponMultiblock<TSubsystem>() {
    override val signText: List<String> = listOf("", "", "", "")

    override val name: String = javaClass.simpleName
    override val advancement: SLAdvancement? = null

    override fun matchesSign(lines: Array<String>): Boolean {
        return false
    }

    override fun matchesUndetectedSign(sign: Sign): Boolean {
        return false
    }
}
