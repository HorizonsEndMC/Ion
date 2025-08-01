package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LogisticTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LogisticTurretProjectile
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material.GRINDSTONE
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

sealed class LogisticTurretMultiblock : TurretMultiblock() {

    override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
        return LogisticTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
    }

    override val displayName: Component get() = text("Cycle Turret (${if (getSign() == 1) "Top" else "Bottom"})")
    override val description: Component get() = text("Rotating weapon system that heals other starships.")

    protected abstract fun getSign(): Int

    override fun getBalancing(starship: ActiveStarship): StarshipWeapons.StarshipWeapon = starship.balancing.weapons.logisticTurret

    override fun buildFirePointOffsets(): List<Vec3i> = listOf(Vec3i(0, +4 * getSign(), +2))

    override fun MultiblockShape.buildStructure() {
        z(-1) {
            y(getSign() * 3) {
                x(-1).anyStairs()
                x(+0).terracottaOrDoubleSlab()
                x(+1).anyStairs()
            }
            y(getSign() * 4) {
                x(+0).anyStairs()
            }
        }
        z(+0) {
            y(getSign() * 2) {
                x(+0).sponge()
            }
            y(getSign() * 3) {
                x(-1).terracottaOrDoubleSlab()
                x(+0).emeraldBlock()
                x(+1).terracottaOrDoubleSlab()
            }
            y(getSign() * 4) {
                x(-1).anySlab()
                x(+0).type(GRINDSTONE)
                x(+1).anySlab()
            }
        }
        z(+1) {
            y(getSign() * 3) {
                x(-1).anyStairs()
                x(+0).terracottaOrDoubleSlab()
                x(+1).anyStairs()
            }
            y(getSign() * 4) {
                x(+0).endRod()
            }
        }
    }

    override fun shoot(world: World, pos: Vec3i, face: BlockFace, dir: Vector, starship: ActiveStarship, shooter: Damager, subSystem: TurretWeaponSubsystem, isAuto: Boolean) {
        val speed = getProjectileSpeed(starship)

        for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
            if (starship.isInternallyObstructed(point, dir)) continue

            val loc = point.toLocation(world).toCenterLocation()

            LogisticTurretProjectile(
                starship,
				subSystem.getName(),
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
                starship.balancing.weapons.logisticTurret.soundFireNear,
                starship.balancing.weapons.logisticTurret.soundFireFar,
                starship.balancing.weapons.logisticTurret, // Not used by anything
                shooter,
				DamageType.GENERIC
            ).fire()
        }
    }
}

object TopLogisticTurretMultiblock : LogisticTurretMultiblock() {
    override fun getSign(): Int = 1
    override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomLogisticTurretMultiblock : LogisticTurretMultiblock() {
    override fun getSign(): Int = -1
    override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
