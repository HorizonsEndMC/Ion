package net.starlegacy.feature.multiblock.starshipweapon.heavy

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.secondary.TorpedoWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

object TorpedoStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<TorpedoWeaponSubsystem>() {
    override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TorpedoWeaponSubsystem {
        return TorpedoWeaponSubsystem(starship, pos, face)
    }

    override fun MultiblockShape.buildStructure() {
        at(+0, +0, +0).sponge()
        at(+0, +0, +1).sponge()
        at(+0, +0, +2).dispenser()
    }
}
