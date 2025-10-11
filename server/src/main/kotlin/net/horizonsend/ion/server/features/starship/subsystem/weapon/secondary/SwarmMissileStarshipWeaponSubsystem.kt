package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.starship.SwarmMissileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.SwarmMissleStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.TopSwarmMissileStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.BoidProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SwarmMissileProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.kyori.adventure.text.Component
import org.bukkit.Material
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
    override val boostChargeNanos: Long get() = balancing.boostChargeNanos

    override fun getName(): Component = Component.text("Swarm Missile")

    override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
        return dir
    }

    private fun getFirePos(): Vec3i {
        val (right, up, forward) = multiblock.getFirePointOffset()
        return getRelative(pos, face, right, up, forward)
    }

    override fun canFire(dir: Vector, target: Vector): Boolean {
        return !starship.isInternallyObstructed(multiblock.getFirePointOffset(), face.direction)
    }

    override fun isIntact(): Boolean {
		val block = pos.toLocation(starship.world).block
		val inward = if (face in arrayOf(BlockFace.UP, BlockFace.DOWN)) BlockFace.NORTH else face
		return multiblock.blockMatchesStructure(block, inward)
    }


    override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
        val otherMissiles = mutableListOf<BoidProjectile<*>>()
        (0 until 9).forEach { _ ->
            SwarmMissileProjectile(StarshipProjectileSource(starship), getName(), getFirePos().toLocation(starship.world), dir, shooter, otherMissiles, TopSwarmMissileStarshipWeaponMultiblock.damageType).fire()
        }
    }

    override fun isRequiredAmmo(item: ItemStack): Boolean = requireMaterial(item, Material.COAL_BLOCK, 2)

    override fun consumeAmmo(itemStack: ItemStack) {
        consumeItem(itemStack, 2)
    }
}