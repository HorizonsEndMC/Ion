package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.DisintegratorBeamWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DisintegratorBeamProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.util.Vector

class DisintegratorBeamWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    override var face: BlockFace,
    val multiblock: DisintegratorBeamWeaponMultiblock
) : WeaponSubsystem(starship, pos), DirectionalSubsystem, ManualWeaponSubsystem {

    companion object {
        private const val MIN_STACKS = 1
        private const val MAX_STACKS = 20
    }

    override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.disintegratorBeam
    override val powerUsage: Int = balancing.powerUsage
    private val range: Double = balancing.range
    private val inaccuracyRadians = balancing.inaccuracyRadians

    // amount of "stacks" that the weapon has; more stacks = more damage
    var lastImpact: Long = System.nanoTime()
    // beam stacks always between MIN_STACKS and MAX_STACKS
    var beamStacks: Int = 1
        set(value) { field = value.coerceIn(MIN_STACKS, MAX_STACKS) }

    private fun getSign() = starship.world.getBlockAtKey(pos.toBlockKey()).getState(false) as? Sign

    fun getFirePos() = Vec3i(pos.x + face.modX * 6, pos.y + face.modY * 6, pos.z + face.modZ * 6)

    override fun canFire(dir: Vector, target: Vector): Boolean {
        return !starship.isInternallyObstructed(getFirePos(), dir)
    }

    override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
        val origin = getFirePos().toCenterVector()
        val adjustedDir = target.clone().subtract(origin)

        val horizontalAxis = adjustedDir.clone()
        horizontalAxis.y = 0.0
        horizontalAxis.rotateAroundY(90.0)
        horizontalAxis.normalize()

        adjustedDir.rotateAroundAxis(horizontalAxis, Math.toRadians(randomDouble(-inaccuracyRadians, inaccuracyRadians)))
        adjustedDir.rotateAroundY(Math.toRadians(randomDouble(-inaccuracyRadians, inaccuracyRadians)))

        return adjustedDir.normalize()
    }

    override fun isIntact(): Boolean {
        val sign = getSign() ?: return false
        return multiblock.signMatchesStructure(sign, loadChunks = true, particles = false)
    }

    override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
        lastFire = System.nanoTime()

        val loc = getFirePos().toCenterVector().toLocation(starship.world)
        DisintegratorBeamProjectile(starship, getName(), loc, dir, range, starship.controller.damager, this, damageCalculation()).fire()
    }

    override fun getMaxPerShot(): Int = balancing.maxPerShot

    // increase damage based on current stacks
    private fun damageCalculation(): Double {
        return when (beamStacks) {
            in 1 .. 5 -> 0.5
            in 6 .. 10 -> 1.0
            in 11 .. 15 -> 1.5
            in 16 .. 20 -> 3.5
            else -> 0.5
        }
    }

	override fun getName(): Component {
		return Component.text("Disintegratior Beam")
	}
}
