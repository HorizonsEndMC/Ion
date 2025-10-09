package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.starship.TestBoidCannonBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.TestBoidCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.BoidProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TestBoidProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class TestBoidWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
) : CannonWeaponSubsystem<TestBoidCannonBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(TestBoidWeaponSubsystem::class)) {
    override val length: Int = 3

    override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
        val projectileList = mutableListOf<BoidProjectile<*>>()
        for (newBoid in 0 until 9) {
            val randomDir = dir.clone()
                .rotateAroundX(randomDouble(-0.15, 0.15))
                .rotateAroundY(randomDouble(-0.15, 0.15))
                .rotateAroundZ(randomDouble(-0.15, 0.15))
            val randomLoc = loc.clone()
                .add(randomDir.clone().normalize().multiply(0.1))

            TestBoidProjectile(
                StarshipProjectileSource(starship),
                getName(),
                randomLoc,
                randomDir,
                shooter,
                projectileList,
                TestBoidCannonStarshipWeaponMultiblock.damageType
            ).fire()
        }
    }

    override fun getName(): Component {
        return Component.text("Test Boid Cannon")
    }

}