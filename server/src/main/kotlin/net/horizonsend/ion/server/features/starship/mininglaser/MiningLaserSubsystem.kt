package net.horizonsend.ion.server.features.starship.mininglaser

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.mininglaser.multiblock.MiningLaserMultiblock
import net.horizonsend.ion.server.miscellaneous.extensions.alert
import net.horizonsend.ion.server.miscellaneous.extensions.information
import net.horizonsend.ion.server.miscellaneous.runnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.drills.DrillMultiblock
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import net.starlegacy.util.toLocation
import org.bukkit.Bukkit.getPlayer
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class MiningLaserSubsystem(
	override val starship: ActivePlayerStarship,
	pos: Vec3i,
	private val face: BlockFace,
	val multiblock: MiningLaserMultiblock
) : WeaponSubsystem(starship, pos), ManualWeaponSubsystem {
	private val firingTasks = mutableListOf<BukkitTask>()
	var isFiring = false
	lateinit var targetedBlock: Vector

	// Const disabled sign text
	private val DISABLED = Component.text("[DISABLED]").color(NamedTextColor.RED)

	// Starship power usage, 0
	override val powerUsage: Int = 0

	// Power used per block broken
	private val blockBreakPowerUsage: Double = 6.0

	// Save some calc time
	private val radiusSquared = multiblock.mineRadius * multiblock.mineRadius

	var tick = 0

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		val firePos = getFirePos()
		val vector = target.clone().subtract(firePos.toVector())

		return firePos.toVector().add(vector.clone().normalize().multiply(multiblock.range))
	}

	private fun setUser(sign: Sign, player: String?) {
		val line3 = player?.let { Component.text(player) } ?: DISABLED
		sign.line(3, line3)
		sign.update(false, false)
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !starship.isInternallyObstructed(getFirePos(), dir)
	}

	private fun getFirePos(): Vec3i {
		val (x, y, z) = multiblock.getFirePointOffset()
		val facing = getSign()?.getFacing() ?: face
		val right = facing.rightFace

		return Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = y,
			z = (right.modZ * x) + (facing.modZ * z)
		)
	}

	private fun getSign() = starship.serverLevel.world.getBlockAt(pos.x, pos.y, pos.z).getState(false) as? Sign

	override fun isIntact(): Boolean {
		val sign = getSign() ?: return false
		return multiblock.signMatchesStructure(sign, loadChunks = true, particles = false)
	}

// 	// TODO use this for the multiple guardian beams
// 	private fun getPoints(axis: Vector): List<Location> {
// 		val spread: Double = 360.0 / multiblock.beamCount
// 		val points = mutableListOf<Location>()
// 		val start = axis.clone().normalize().rotateAroundZ(90.0).multiply(multiblock.mineRadius).add(axis.clone())
//
// 		for (count in multiblock.beamCount.downTo(1)) {
// 			val newLoc = start.rotateAroundNonUnitAxis(axis.clone(), spread * count)
// 			points.add(newLoc.toLocation(starship.serverLevel.world))
// 		}
//
// 		return points
// 	}

	override fun manualFire(shooter: Player, dir: Vector, target: Vector) {
		val sign = getSign() ?: return

		// Calculate a vector in the direction from the fire point to the targeted block
		val vectorToTarget =
			target.clone().subtract((getFirePos() + pos).toVector()).normalize().multiply(multiblock.range)

		// Add this vector to the fire position to find the position in the direction at max range.
		this.targetedBlock = (getFirePos() + pos).toVector().add(vectorToTarget)
		setFiring(!isFiring, sign, shooter)

		// If it is within range, the raycast will move it forward.
	}

	private fun setFiring(firing: Boolean, sign: Sign, user: Player? = null) {
		isFiring = firing

		when (firing) {
			true -> {
				setUser(sign, user!!.name)
				starship.information("Enabled mining laser at $pos")
				startFiringSequence()
			}

			false -> {
				setUser(sign, null)
				starship.information("Disabled mining laser at $pos")
				starship
				cancelTask()
			}
		}
	}

	private fun startFiringSequence() {
		val fireTask =
			runnable {
				if (isFiring) {
					fire()
				} else {
					cancel()
				}
			}.runTaskTimer(IonServer, 0L, 5L)

		starship.serverLevel.world.playSound(
			multiblock.getFirePointOffset().plus(pos).toLocation(starship.serverLevel.world),
			"starship.weapon.mining_laser.start",
			1f,
			1f
		)

		firingTasks.add(fireTask)
	}

	private fun cancelTask() {
		isFiring = false
		firingTasks.forEach { it.cancel() }
		firingTasks.clear()

		// Stop sound
		for (player in starship.centerOfMass.toLocation(starship.serverLevel.world).world.players) {
			if (
				player.location.distance(
					starship.centerOfMass.toLocation(starship.serverLevel.world)
				) > multiblock.range
			) {
				continue
			}

			starship.serverLevel.world.playSound(
				player.location,
				"starship.weapon.mining_laser.stop",
				1.0f,
				1f
			)
		}
	}

	fun fire() {
		// Cancel if
		val sign = getSign() ?: return cancelTask()

		if (!ActiveStarships.isActive(starship)) {
			setFiring(false, sign)
			return
		}

		val user = getPlayer((sign.line(3) as TextComponent).content()) ?: return setFiring(false, sign)
		val power = PowerMachines.getPower(sign, true)

		if (power == 0) {
			starship.alert("Mining Laser at ${sign.block.x}, ${sign.block.y}, ${sign.block.z} ran out of power and was disabled!")

			setFiring(false, sign)
			return
		}

		val initialPos = getFirePos().toLocation(starship.serverLevel.world).toCenterLocation().add(pos.toVector())
		val targetVector = targetedBlock.clone().subtract(initialPos.toVector())

		if (starship.isInternallyObstructed(getFirePos(), targetVector)) {
			starship.sendMessage( // TODO make this work
				Component.text("Mining Laser at ${sign.block.x}, ${sign.block.y}, ${sign.block.z} became obstructed and was disabled!")
			)
		}

		// Ray trace to get the hit position
		val raytrace = starship.serverLevel.world.rayTrace(
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
		val laserEnd = targetedBlock.toLocation(starship.serverLevel.world)
		CrystalLaser(initialPos, laserEnd, 5, -1).durationInTicks().apply { start(IonServer) }

		val blocks = getBlocksToDestroy(laserEnd.block)
		val blocksBroken = DrillMultiblock.breakBlocks(
			sign = sign,
			maxBroken = multiblock.maxBroken,
			toDestroy = blocks,
			output = multiblock.getOutput(sign),
			people = starship.passengerIDs.mapNotNull(::getPlayer).toTypedArray(),
			player = user
		)

		if (blocksBroken > 0) {
			PowerMachines.setPower(sign, power - (blockBreakPowerUsage * blocksBroken).toInt(), true)
		}

		// Sound is 5 seconds, ticks every quarter second
		if (tick % 20 == 0) {
			val soundOrigin = getFirePos().plus(pos).toLocation(starship.serverLevel.world)

			soundOrigin.world.players.forEach {
				if (it.location.distance(soundOrigin) < multiblock.range * 2) {
					it.stopSound(multiblock.sound)

					soundOrigin.world.playSound(it.location, multiblock.sound, SoundCategory.PLAYERS, 1.0f, 1.0f)
				}
			}

			tick = 0
		}

		laserEnd.world.spawnParticle(Particle.EXPLOSION_HUGE, laserEnd, 1)
		tick++
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

					if (
						listOf(
							Material.AIR,
							Material.BEDROCK,
							Material.REINFORCED_DEEPSLATE
						).any { it == toExplode.type }
					) {
						continue
					}

					toDestroy.add(toExplode)
				}
			}
		}

		return toDestroy.apply { sortBy { it.location.distanceSquared(pos.toLocation(center.world)) } }
	}
}
