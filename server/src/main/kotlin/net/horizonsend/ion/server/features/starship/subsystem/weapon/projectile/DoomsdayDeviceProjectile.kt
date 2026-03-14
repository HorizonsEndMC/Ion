package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.starship.DoomsdayDeviceBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.DoomsdayDeviceWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.EntityDamager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.destruction.SinkAnimation
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DoomsdayDeviceWeaponSubsystem.Companion.WARM_UP_TIME_SECONDS
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.features.transport.items.util.DYEABLE_CUBE_MONO
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.iterateVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.exp
import kotlin.math.roundToInt

class DoomsdayDeviceProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile<DoomsdayDeviceBalancing.DoomsdayDeviceProjectileBalancing>(source, name, loc, dir, shooter, DoomsdayDeviceWeaponMultiblock.damageType) {
    private val greenParticleData = Particle.DustTransition(
        shooter.color,
        Color.BLACK,
        balancing.particleThickness.toFloat()
    )

    private val yellowParticleData = Particle.DustTransition(
        shooter.color,
        Color.BLACK,
        balancing.particleThickness.toFloat()
    )

    override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {


        Location(location.world, x, y, z).spherePoints(3.0, 20).forEach {
            it.world.spawnParticle(
                Particle.DUST_COLOR_TRANSITION,
                it.x,
                it.y,
                it.z,
                1,
                0.5,
                0.5,
                0.5,
                2.0,
                greenParticleData,
                force
            )
        }

        Tasks.syncDelay(5) {
            Location(location.world, x, y, z).spherePoints(1.5, 5).forEach {
                it.world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    it.x,
                    it.y,
                    it.z,
                    1,
                    0.25,
                    0.25,
                    0.25,
                    2.0,
                    yellowParticleData,
                    force
                )
            }
        }
    }

    // overriding the entire tick() function just to change the raySize :weary:
    override fun tick() {
        delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds

        val predictedNewLoc = location.clone().add(direction.clone().multiply(delta * speed))
        if (!predictedNewLoc.isChunkLoaded) {
            return
        }
        val result: RayTraceResult? = location.world.rayTrace(location, direction, delta * speed, FluidCollisionMode.NEVER, true, 0.5) { it.type != EntityType.ITEM_DISPLAY }
        val newLoc = result?.hitPosition?.toLocation(location.world) ?: predictedNewLoc
        val travel = location.distance(newLoc)

        moveVisually(location, newLoc, travel)

        var impacted = false

        if (result != null) {
            impacted = tryImpact(result, newLoc)
        }

        location = newLoc

        distance += travel

        if (impacted) {
            return
        }

        if (distance >= range) {
            return
        }

        lastTick = System.nanoTime()
        reschedule()
    }

	private var animationAge = 0

	override fun fire() {
		repeat(3) {
			DoomsdayDeviceProjectileAnimation(location.toVector(), Vector(), shooter.color, 0.25, 12.0, 1.0).schedule()
		}
		super.fire()
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val line = oldLocation.alongVector(newLocation.toVector().subtract(oldLocation.toVector()), 3)

		for (spawnLoc in line) {
			if (spawnLoc == line.last()) continue

			DoomsdayDeviceProjectileAnimation(
				spawnLoc.toVector(),
				direction.clone().normalize(),
				shooter.color,
				1.0,
				1.0,
				12.0
			).schedule()
		}

		animationAge++
	}

    override fun onHitEntity(entity: LivingEntity) {
        when (shooter) {
            is PlayerDamager -> entity.damage(10000.0, shooter.player)
            is EntityDamager -> entity.damage(10000.0, shooter.entity)
            else -> entity.damage(10000.0)
        }
    }

    override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
        super.impact(newLoc, block, entity)

		newLoc.world.spawnParticle(
            Particle.LAVA,
            newLoc.x,
            newLoc.y,
            newLoc.z,
            200,
            3.0,
            3.0,
            3.0,
            0.0,
            null,
            true
        )

        newLoc.world.spawnParticle(
            Particle.WHITE_ASH,
            newLoc.x,
            newLoc.y,
            newLoc.z,
            250,
            5.0,
            5.0,
            5.0,
            0.0,
            null,
            true
        )

        newLoc.world.spawnParticle(
            Particle.ASH,
            newLoc.x,
            newLoc.y,
            newLoc.z,
            200,
            5.0,
            5.0,
            5.0,
            0.0,
            null,
            true
        )

        for (point in newLoc.spherePoints(15.0, 20)) {
            newLoc.iterateVector(Vector(point.x - newLoc.x, point.y - newLoc.y, point.z - newLoc.z), 10) { pointAlong, _ ->
                pointAlong.world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    pointAlong.x,
                    pointAlong.y,
                    pointAlong.z,
                    1,
                    0.5,
                    0.5,
                    0.5,
                    2.0,
                    greenParticleData,
                    true
                )
            }
        }

        for (point in newLoc.spherePoints(30.0, 10)) {
            newLoc.iterateVector(Vector(point.x - newLoc.x, point.y - newLoc.y, point.z - newLoc.z), 20) { pointAlong, _ ->
                pointAlong.world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    pointAlong.x,
                    pointAlong.y,
                    pointAlong.z,
                    1,
                    0.5,
                    0.5,
                    0.5,
                    2.0,
                    yellowParticleData,
                    true
                )
            }
        }
    }

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		super.onImpactStarship(starship, impactLocation)

		val explosionSize = 25.0f
		val offsetDirection = direction.clone().multiply(5.0)
		val explosionLocation = impactLocation.clone().add(offsetDirection)

		Tasks.syncDelay(10L) {
			explosionLocation.createExplosion(explosionSize)

			// explosionOccurred only controls the hull hitmarker sound; just use this to increase damager points on the target
			addToDamagers(
				explosionLocation.world,
				explosionLocation.block,
				shooter,
				explosionSize.roundToInt(),
				explosionOccurred = false,
				runStarshipImpactEvent = false
			)

		}
	}

	override fun playCustomSound(loc: Location, nearSound: SoundInfo, farSound: SoundInfo) { /* Do nothing */ }


	inner class DoomsdayDeviceProjectileAnimation(
		origin: Vector,
		moveDir: Vector,
		color: Color,
		speed: Double,
		initialScale: Double,
		finalScale: Double,
	) : BukkitRunnable() {
		val item = DYEABLE_CUBE_MONO.construct { t -> t.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color, false)) }
		val rotationAxis = Vector.getRandom()

		val block = object : SinkAnimation.ColoredSinkAnimationBlock(
			duration = (3 * 20 * speed).toLong(),
			wrapper = ItemDisplayContainer(
				world = location.world,
				initPosition = origin,
				initHeading = Vector.getRandom(),
				initScale = 1.0f,
				item = item,
			),
			direction = moveDir.clone().multiply(0.5),
			initialScale = initialScale,
			finalScale = finalScale,
			rotationAxis = rotationAxis,
			rotationDegrees = 0.5,
			colors = mapOf(
				color to 5,
				Color.BLACK to 2,
			),
			motionAdjuster = {}
		) {}

		override fun run() {
			block.update()
			if (block.checkDead()) cancel()
		}

		fun schedule() = runTaskTimerAsynchronously(IonServer, 1L, 1L)
	}
}
