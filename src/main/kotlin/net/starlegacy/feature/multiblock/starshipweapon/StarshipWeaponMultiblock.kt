package net.starlegacy.feature.multiblock.starshipweapon

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

abstract class StarshipWeaponMultiblock<TSubsystem : WeaponSubsystem> : Multiblock() {
    override val advancement: SLAdvancement? = null

    abstract fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TSubsystem
}
