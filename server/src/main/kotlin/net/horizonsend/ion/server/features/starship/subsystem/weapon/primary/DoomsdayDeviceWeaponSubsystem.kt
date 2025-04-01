package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DoomsdayDeviceProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class DoomsdayDeviceWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
) : CannonWeaponSubsystem<StarshipWeapons.DoomsdayDeviceBalancing>(starship, pos, face, starship.balancingManager.getSupplier()), HeavyWeaponSubsystem {
	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

    companion object {
        private const val WARM_UP_TIME_SECONDS = 3
    }

    override val length: Int = 7

    override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
        var tick = 0
        runnable {

            if (tick > (WARM_UP_TIME_SECONDS * 20 / 5)) cancel()

            val newFirePos = getFirePos()

            val data = Particle.DustTransition(
                Color.fromARGB(255, 182, 255, 0),
                Color.BLACK,
                balancing.projectile.particleThickness.toFloat()
            )

            // min radius: 1; max radius: 6
            newFirePos.toLocation(loc.world).spherePoints(((5.0 / WARM_UP_TIME_SECONDS.toDouble() * (tick / 5)) + 1), 20).forEach {
                it.world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    it.x,
                    it.y,
                    it.z,
                    1,
                    0.5,
                    0.5,
                    0.5,
                    0.0,
                    data,
                    true
                )
            }

            tick += 1
        }.runTaskTimer(IonServer, 0L, 5L)

        Tasks.syncDelay(20 * WARM_UP_TIME_SECONDS.toLong()) {
            val newFirePos = getFirePos()
            DoomsdayDeviceProjectile(StarshipProjectileSource(starship), getName(), newFirePos.toLocation(loc.world), dir, shooter).fire()
        }
    }

	override fun getName(): Component {
		return Component.text("Doomsday Device")
	}
}

