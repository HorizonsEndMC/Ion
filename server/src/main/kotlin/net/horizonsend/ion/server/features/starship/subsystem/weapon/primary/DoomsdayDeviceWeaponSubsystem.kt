package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.starship.DoomsdayDeviceBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.destruction.SinkAnimation
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DoomsdayDeviceProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.features.transport.items.util.EXPLOSION_RING
import net.horizonsend.ion.server.miscellaneous.playDirectionalStarshipSound
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class DoomsdayDeviceWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
) : CannonWeaponSubsystem<DoomsdayDeviceBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(DoomsdayDeviceWeaponSubsystem::class)), HeavyWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

    companion object {
        private const val WARM_UP_TIME_SECONDS = 4
    }

    override val length: Int = 11

    override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		toPlayersInRadius(loc, balancing.projectile.range * 20.0) { player ->
			playDirectionalStarshipSound(loc, player, balancing.projectile.fireSoundNear, balancing.projectile.fireSoundNear, balancing.projectile.range)
		}
		var tick = 0
        runnable {

            if (tick > (WARM_UP_TIME_SECONDS * 20 / 5)) cancel()

            val newFirePos = getFirePos()

            val data = Particle.DustTransition(
                shooter.color,
                Color.BLACK,
                balancing.projectile.particleThickness.toFloat()
            )

            newFirePos.toLocation(loc.world).spherePoints((tick / 2 + 1).toDouble(), 40).forEach {
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

			val stopPoint = getFireVec().clone().add(Vector(face.modX * 10.0, face.modY * 10.0, face.modZ * 10.0)).toLocation(loc.world)
			val furtherStopPoint = stopPoint.add(face.modX * 10.0, face.modY * 10.0, face.modZ * 10.0)

			if (tick < ((WARM_UP_TIME_SECONDS - 1) * 4)) {
				newFirePos.toLocation(loc.world).circlePoints(20.0, 100, face.direction).shuffled().take(50).forEach {
					it.world.spawnParticle(
						Particle.TRAIL,
						it,
						1,
						0.0,
						0.0,
						0.0,
						0.0,
						Particle.Trail(stopPoint, Color.WHITE, 20),
						true
					)
				}

				newFirePos.toLocation(loc.world).circlePoints(40.0, 500, face.direction).shuffled().take(100).forEach {
					it.world.spawnParticle(
						Particle.TRAIL,
						it,
						1,
						0.0,
						0.0,
						0.0,
						0.0,
						Particle.Trail(furtherStopPoint, Color.WHITE, 20),
						true
					)
				}
			}

            tick += 1
        }.runTaskTimer(IonServer, 0L, 5L)

        Tasks.syncDelay(20 * WARM_UP_TIME_SECONDS.toLong()) {
            val newFirePos = getFirePos()
            DoomsdayDeviceProjectile(StarshipProjectileSource(starship), getName(), newFirePos.toLocation(loc.world), dir, shooter).fire()
			DoomsdayDeviceFireShockwaveAnimation().schedule()
        }
    }

	override fun getName(): Component {
		return Component.text("Doomsday Device")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireCustomItem(item, CustomItemKeys.STELLAR_PRISM_LOADED.getValue(), 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}

	inner class DoomsdayDeviceFireShockwaveAnimation() : BukkitRunnable() {
		private val shockwaveItem = object : SinkAnimation.ColoredSinkAnimationBlock(
			duration = 100,
			wrapper = ItemDisplayContainer(
				world = starship.world,
				initPosition = getFireVec(),
				initHeading = face.direction,
				initScale = 1.0f,
				item = EXPLOSION_RING.construct { t -> t.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.WHITE)) },
			),
			direction = Vector(),
			initialScale = 1.0,
			finalScale = 50.0,
			rotationAxis = BlockFace.NORTH.direction,
			rotationDegrees = 0.0,
			colors = mapOf(
				Color.WHITE to 4,
				Color.BLACK to 1
			)
		) {}

		override fun run() {
			shockwaveItem.update()
			if (shockwaveItem.checkDead()) cancel()
		}

		fun schedule() = runTaskTimerAsynchronously(IonServer, 1L, 1L)
	}
}

