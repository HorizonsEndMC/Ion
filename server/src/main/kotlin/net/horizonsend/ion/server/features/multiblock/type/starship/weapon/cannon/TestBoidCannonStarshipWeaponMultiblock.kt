package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.TestBoidWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace

object TestBoidCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<TestBoidWeaponSubsystem>(), DisplayNameMultilblock {
    override val key: String = "test_boid_cannon"
    override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TestBoidWeaponSubsystem {
        return TestBoidWeaponSubsystem(starship, pos, face)
    }

    override val displayName: Component
        get() = Component.text("Test Boid Cannon")
    override val description: Component
        get() = Component.text("Test boid cannon")

    override fun MultiblockShape.buildStructure() {
        at(+0, +0, +0).sponge()
        at(+0, +0, +1).emeraldBlock()
        at(+0, +0, +2).furnace()
    }
}