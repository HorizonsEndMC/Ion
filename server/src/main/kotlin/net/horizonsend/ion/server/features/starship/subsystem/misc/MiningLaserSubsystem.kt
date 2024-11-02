package net.horizonsend.ion.server.features.starship.subsystem.misc

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.alertSubtitle
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorSubtitle
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.event.build.StarshipBreakBlockEvent
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.MultiblockEntitySubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.enumSetOf
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class MiningLaserSubsystem(
    override val starship: ActiveControlledStarship,
	override val entity: MiningLaserMultiblock.MiningLaserMultiblockEntity,
) : WeaponSubsystem(starship, entity.vec3i), ManualWeaponSubsystem, DirectionalSubsystem, MultiblockEntitySubsystem {

	val multiblock = entity.multiblock
	override var face: BlockFace = entity.structureDirection

	override val balancing: StarshipWeapons.StarshipWeapon = StarshipWeapons.StarshipWeapon(
		range = 0.0,
		speed = 0.0,
		areaShieldDamageMultiplier = 0.0,
		starshipShieldDamageMultiplier = 0.0,
		particleThickness = 0.0,
		explosionPower = 0.0f,
		volume = 0,
		pitch = 0.0f,
		soundName = "",
		powerUsage = 0,
		length = 0 ,
		angleRadiansHorizontal = 0.0,
		angleRadiansVertical = 0.0,
		convergeDistance = 0.0,
		extraDistance = 0,
		fireCooldownMillis = 0,
		boostChargeSeconds = 0,
		aimDistance = 0,
		applyCooldownToAll = false
	)

	private val firingTasks = mutableListOf<BukkitTask>()
	private var isFiring = false
	lateinit var targetedBlock: Vector

	// Power used per block broken
	private val blockBreakPowerUsage: Double = 9.0

	private val radiusSquared = multiblock.mineRadius * multiblock.mineRadius

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		val firePos = entity.getFirePos()
		val vector = target.clone().subtract(firePos.toVector())

		return firePos.toVector().add(vector.clone().normalize().multiply(multiblock.range))
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !starship.isInternallyObstructed(entity.getFirePos(), dir)
	}

	override fun isIntact(): Boolean {
		return entity.isIntact(checkSign = true)
	}

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		// Calculate a vector in the direction from the fire point to the targeted block
		val vectorToTarget = target.clone().subtract((entity.getFirePos()).toVector()).normalize().multiply(multiblock.range)

		// Add this vector to the fire position to find the position in the direction at max range.
		this.targetedBlock = (entity.getFirePos()).toVector().add(vectorToTarget)
		setFiring(!isFiring)

		// If it is within range, the raycast will move it forward.
	}

	private fun setFiring(firing: Boolean) {
		val alreadyFiring = starship.subsystems.filterIsInstance<MiningLaserSubsystem>().count { it.isFiring }

		if (firing) {
			// Less than but not equal to because it will increase by 1
			if (alreadyFiring < starship.type.maxMiningLasers) {
				isFiring = true
				starship.informationAction("Enabled mining laser at $pos")
				startFiringSequence()
			} else {
				starship.information("Your ship can only fire ${starship.type.maxMiningLasers} mining lasers at once!")
				isFiring = false
			}

			return
		}

		isFiring = false
		starship.informationAction("Disabled mining laser at $pos")
		starship
		cancelTask()
	}

	var tick = 0

	private fun startFiringSequence() {
		tick = 0

		val fireTask = runnable {
			if (isFiring) {
				fire()
			} else {
				cancel()
			}
		}.runTaskTimer(IonServer, 0L, 5L)

		starship.world.players
			.filter { it.location.distance(multiblock.getFirePointOffset().plus(pos).toLocation(starship.world)) < multiblock.range * 2 }
			.forEach {

			it.stopSound(multiblock.sound)

			starship.world.playSound(
				it.location,
				"horizonsend:starship.weapon.mining_laser.start",
				SoundCategory.PLAYERS,
				1.0f,
				1.0f
			)
		}

		firingTasks.add(fireTask)
	}

	private fun cancelTask() {
		isFiring = false
		firingTasks.forEach { it.cancel() }
		firingTasks.clear()

		// Stop sound
		for (player in starship.world.players) {
			if (player.location.distance(starship.centerOfMass.toLocation(starship.world)) > multiblock.range) continue

			starship.world.playSound(
				player.location,
				"horizonsend:starship.weapon.mining_laser.stop",
				SoundCategory.PLAYERS,
				1.0f,
				1f
			)
		}
	}

	fun fire() {
		val initialPos = entity.getFirePos().toLocation(starship.world).toCenterLocation()
		val targetVector = targetedBlock.clone().subtract(initialPos.toVector())
		val controller = starship.controller

		if (!ActiveStarships.isActive(starship)) return setFiring(false)

		if (!starship.world.ion.hasFlag(WorldFlag.ALLOW_MINING_LASERS)) {
			starship.userError("The Mining Laser at $pos wasn't able to initialize its gravitational collection beam and was disabled! (Move to a space world)")
			return setFiring(false)
		}

		val power = entity.powerStorage.getPower()

		if (power == 0) {
			starship.alertSubtitle("Mining Laser at $pos ran out of power and was disabled!")

			return setFiring(false)
		}

		// Ray trace to get the hit position
		val raytrace = starship.world.rayTrace(
			initialPos,
			targetVector.clone(),
			multiblock.range,
			FluidCollisionMode.NEVER,
			true,
			0.1,
			null
		)

		// Set the targeted block to the nearest hit block (if not null)
		raytrace?.hitPosition?.let { targetedBlock = it }

		// Create a laser to visualize the beam with a life of 5 ticks
		val laserEnd = targetedBlock.toLocation(starship.world)
		val laser = CrystalLaser(initialPos, laserEnd, 5, -1).durationInTicks()
		laser.start(IonServer)

		val blocks = getBlocksToDestroy(laserEnd.block)

		if (blocks.any { starship.contains(it.x, it.y, it.z) }) {
			starship.alert("Mining Laser at $pos became obstructed and was disabled!")
			laser.stop()
			return setFiring(false)
		}

		if (AreaShields.getNearbyAreaShields(laserEnd, multiblock.mineRadius.toDouble()).isNotEmpty()) {
			starship.alert("Mining Laser at $pos targeted an area shield and was disabled!")
			laser.stop()
			return setFiring(false)
		}

		val blocksBroken = DrillMultiblock.breakBlocks(
			maxBroken = multiblock.maxBroken,
			toDestroy = blocks,
			output = entity.getOutput() ?: run { setFiring(false); return starship.alert("Mining Laser at $pos's output inventory could not be found!") },
			{ controller.canDestroyBlock(it) && StarshipBreakBlockEvent(controller, it).callEvent() },
			{ starship.userErrorSubtitle("Not enough space!"); laser.stop() }
		)

		if (blocksBroken > 0) {
			entity.powerStorage.removePower((blockBreakPowerUsage * blocksBroken).toInt())
			laserEnd.world.spawnParticle(Particle.EXPLOSION, laserEnd, 1)
		} else {
			starship.sendActionBar(text("Mining laser is trying to break air!", NamedTextColor.RED))
		}

		playBeamSound()

		tick++
	}

	private fun playBeamSound() {
		// Sound is 5 seconds, ticks every quarter second
		if (tick % 20 == 0) {
			val soundOrigin = entity.getFirePos().plus(pos).toLocation(starship.world)

			soundOrigin.world.players.forEach {
				if (it.location.distance(soundOrigin) < multiblock.range * 2) {
					it.stopSound(multiblock.sound)

					soundOrigin.world.playSound(it.location, multiblock.sound, SoundCategory.PLAYERS, 1.0f, 1.0f)
				}
			}

			tick = 0
		}
	}

	private fun getBlocksToDestroy(center: Block): MutableList<Block> {
		val toDestroy = mutableListOf<Block>()

		val range = IntRange(-multiblock.mineRadius, multiblock.mineRadius)

		for (x in range) {
			val xSquared = x * x

			for (y in range) {
				val ySquared = y * y

				for (z in range) {
					val zSquared = z * z

					if ((xSquared + ySquared + zSquared) >= radiusSquared) continue

					val toExplode = center.getRelative(BlockFace.EAST, x)
						.getRelative(BlockFace.UP, y)
						.getRelative(BlockFace.SOUTH, z)

					if (MINING_LASER_NOT_MINED.contains(toExplode.type)) continue

					toDestroy.add(toExplode)
				}
			}
		}

		toDestroy.sortBy { it.location.distanceSquared(pos.toLocation(center.world)) }

		return toDestroy
	}

	companion object {
		val MINING_LASER_NOT_MINED = enumSetOf(Material.AIR, Material.BEDROCK, Material.REINFORCED_DEEPSLATE, Material.BARRIER)
	}

	override val powerUsage: Int = 0

	override fun getName(): Component {
		return text("Mining Laser [how]")
	}
}
