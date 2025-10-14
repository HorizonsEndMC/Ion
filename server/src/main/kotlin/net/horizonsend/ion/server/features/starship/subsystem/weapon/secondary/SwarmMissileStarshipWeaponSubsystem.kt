package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.starship.SwarmMissileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.BottomSwarmMissileStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.HorizontalSwarmMissileStarshipWeaponMultiblock
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
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
        val fireDir = target.clone()
            .subtract(getFirePos().toCenterVector())
            .normalize()
        val yaw = atan2(-fireDir.x, fireDir.z)
        val pitch = atan(-fireDir.y / sqrt(fireDir.x.pow(2) + fireDir.z.pow(2)))

        val xz = cos(pitch)
        val x = -xz * sin(yaw)
        val y = -sin(pitch)
        val z = xz * cos(yaw)
        return Vector(x, y, z)
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
        val initialLaunchDirection = when (multiblock) {
            is HorizontalSwarmMissileStarshipWeaponMultiblock -> face.direction
            is TopSwarmMissileStarshipWeaponMultiblock -> BlockFace.UP.direction
            is BottomSwarmMissileStarshipWeaponMultiblock -> BlockFace.DOWN.direction
            else -> face.direction
        }

        for (newBoid in 0 until 9) {
            Tasks.syncDelay(newBoid.toLong()) {
                val randomInitialDir = initialLaunchDirection.clone()
                    .rotateAroundX(randomDouble(-0.15, 0.15))
                    .rotateAroundY(randomDouble(-0.15, 0.15))
                    .rotateAroundZ(randomDouble(-0.15, 0.15))
                val randomLoc = getFirePos().toCenterVector().clone()
                    .add(randomInitialDir.clone().normalize().multiply(0.1))

                SwarmMissileProjectile(
                    StarshipProjectileSource(starship),
                    getName(),
                    randomLoc.toLocation(starship.world),
                    dir,
                    randomInitialDir,
                    shooter.color,
                    shooter,
                    otherMissiles,
                    TopSwarmMissileStarshipWeaponMultiblock.damageType
                ).fire()

                (0 until 10).forEach { _ ->
                    val angle = Math.PI / 12
                    val opposite = randomInitialDir.clone()
                        .rotateAroundX(randomDouble(-angle, angle))
                        .rotateAroundY(randomDouble(-angle, angle))
                        .rotateAroundZ(randomDouble(-angle, angle))
                    starship.world.spawnParticle(Particle.CLOUD, randomLoc.toLocation(starship.world), 0, opposite.x, opposite.y, opposite.z, 5.0, null, true)
                }
            }
        }
    }

    override fun isRequiredAmmo(item: ItemStack): Boolean = requireMaterial(item, Material.COAL_BLOCK, 2) ||
            requireMaterial(item, Material.COAL, 18) ||
            requireMaterial(item, Material.CHARCOAL, 36)

    override fun consumeAmmo(itemStack: ItemStack) {
        when (itemStack.type) {
            Material.COAL_BLOCK -> consumeItem(itemStack, 2)
            Material.COAL -> consumeItem(itemStack, 18)
            Material.CHARCOAL -> consumeItem(itemStack, 36)
            else -> throw IllegalArgumentException("Unsupported material type")
        }
    }
}