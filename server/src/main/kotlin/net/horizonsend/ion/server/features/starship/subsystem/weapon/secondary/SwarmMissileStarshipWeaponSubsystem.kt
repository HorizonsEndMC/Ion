package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.starship.SwarmMissileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.SwarmMissleStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class SwarmMissileStarshipWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    override var face: BlockFace,
    private val multiblock: SwarmMissleStarshipWeaponMultiblock,
) : BalancedWeaponSubsystem<SwarmMissileBalancing>(starship, pos, starship.balancingManager.getWeaponSupplier(SwarmMissileStarshipWeaponSubsystem::class)),
    HeavyWeaponSubsystem,
    ManualWeaponSubsystem,
    DirectionalSubsystem,
    AmmoConsumingWeaponSubsystem {

    override fun getName(): Component = Component.text("Swarm Missile")

    override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
        return dir
    }

    override fun canFire(dir: Vector, target: Vector): Boolean {
        TODO("Not yet implemented")
    }

    override fun isIntact(): Boolean {
		val block = pos.toLocation(starship.world).block
		val inward = if (face in arrayOf(BlockFace.UP, BlockFace.DOWN)) BlockFace.NORTH else face
		return multiblock.blockMatchesStructure(block, inward)
    }

    override val boostChargeNanos: Long
        get() = TODO("Not yet implemented")

    override fun manualFire(
        shooter: Damager,
        dir: Vector,
        target: Vector
    ) {
        TODO("Not yet implemented")
    }

    override fun isRequiredAmmo(item: ItemStack): Boolean {
        TODO("Not yet implemented")
    }

    override fun consumeAmmo(itemStack: ItemStack) {
        TODO("Not yet implemented")
    }
}