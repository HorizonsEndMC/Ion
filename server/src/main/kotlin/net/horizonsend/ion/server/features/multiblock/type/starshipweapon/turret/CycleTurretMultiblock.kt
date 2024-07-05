package net.horizonsend.ion.server.features.multiblock.starshipweapon.turret

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.CycleTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.CycleTurretProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

sealed class CycleTurretMultiblock : TurretMultiblock() {

    override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
        return CycleTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
    }

    protected abstract fun getYFactor(): Int

    override fun getBalancing(starship: ActiveStarship): StarshipWeapons.StarshipWeapon = starship.balancing.weapons.cycleTurret

    override fun buildFirePointOffsets(): List<Vec3i> = listOf(
        Vec3i(-2, getYFactor() * 4, +2),
        Vec3i(-1, getYFactor() * 4, +3),
        Vec3i(+1, getYFactor() * 4, +3),
        Vec3i(+2, getYFactor() * 4, +2)
    )

    override fun MultiblockShape.buildStructure() {
        y(getYFactor() * 2) {
            z(-1) {
                x(+0).sponge()
            }

            z(+0) {
                x(-1).sponge()
                x(+1).sponge()
            }

            z(+1) {
                x(+0).sponge()
            }
        }

        y(getYFactor() * 3) {
            z(-3) {
                x(-1).anyStairs()
                x(+0).netheriteCasing()
                x(+1).anyStairs()
            }

            z(-2) {
                x(-2).anySlab()
                x(-1..1) { terracotta() }
                x(+2).anySlab()
            }

            z(-1) {
                x(-3).anyStairs()
                x(-2..2) { terracotta() }
                x(+3).anyStairs()
            }

            z(+0) {
                x(-3).netheriteCasing()
                x(-2..-1) { terracotta() }
                x(+0).enrichedUraniumBlock()
                x(+1..2) { terracotta() }
                x(+3).netheriteCasing()
            }

            z(+1) {
                x(-3).anyStairs()
                x(-2..2) { terracotta() }
                x(+3).anyStairs()
            }

            z(+2) {
                x(-2).anySlab()
                x(-1..1) { terracotta() }
                x(+2).anySlab()
            }

            z(+3) {
                x(-1).anyStairs()
                x(+0).netheriteCasing()
                x(+1).anyStairs()
            }
        }

        y(getYFactor() * 4) {
            z(-2) {
                x(-1).anyStairs()
                x(+0).anyTrapdoor()
                x(+1).anyStairs()
            }

            z(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyStairs()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            z(+0) {
                x(-2).grindstone()
                x(-1).ironBlock()
                x(+0).stainedGlass()
                x(+1).ironBlock()
                x(+2).grindstone()
            }

            z(+1) {
                x(-2).endRod()
                x(-1).grindstone()
                x(+0).anyStairs()
                x(+1).grindstone()
                x(+2).endRod()
            }

            z(+2) {
                x(-1).endRod()
                x(+0).anyTrapdoor()
                x(+1).endRod()
            }
        }
    }

    override fun shoot(
        world: World,
        pos: Vec3i,
        face: BlockFace,
        dir: Vector,
        starship: ActiveStarship,
        shooter: Damager,
        isAuto: Boolean
    ) {
        val speed = getProjectileSpeed(starship)

        for ((index, point) in getAdjustedFirePoints(pos, face).withIndex()) {
            if (starship.isInternallyObstructed(point, dir)) continue

            val loc = point.toLocation(world).toCenterLocation()

            CycleTurretProjectile(
                starship,
                loc,
                dir,
                speed,
                shooter.color,
                getRange(starship),
                getParticleThickness(starship),
                getExplosionPower(starship),
                getStarshipShieldDamageMultiplier(starship),
                getAreaShieldDamageMultiplier(starship),
                getSound(starship),
                starship.balancing.weapons.cycleTurret, // Not used by anything
                shooter,
                index
            ).fire()
        }
    }
}

object TopCycleTurretMultiblock : CycleTurretMultiblock() {
    override fun getYFactor(): Int = 1
    override fun getPilotOffset(): Vec3i = Vec3i(+0, +0, +0)
}

object BottomCycleTurretMultiblock : CycleTurretMultiblock() {
    override fun getYFactor(): Int = -1
    override fun getPilotOffset(): Vec3i = Vec3i(+0, +0, +0)
}