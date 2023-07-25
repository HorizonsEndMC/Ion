package net.horizonsend.ion.server.features.starship.subsystem

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.drills.DrillMultiblock
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblock
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.event.build.StarshipBreakBlockEvent
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class MiningLaserSubsystem(
    override val starship: ActiveControlledStarship,
    pos: Vec3i,
    private val face: BlockFace,
    val multiblock: MiningLaserMultiblock,
) : WeaponSubsystem(starship, pos), ManualWeaponSubsystem {
	private val firingTasks = mutableListOf<BukkitTask>()
	private var isFiring = false
	lateinit var targetedBlock: Vector

	// Const disabled sign text
	private val disabled = text("[DISABLED]").color(NamedTextColor.RED)

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
		val line3 = player?.let { text(player) } ?: disabled
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

	private fun getSign() = starship.world.getBlockAt(pos.x, pos.y, pos.z).getState(false) as? Sign

	override fun isIntact(): Boolean {
		val sign = getSign() ?: return false
		return multiblock.signMatchesStructure(sign, loadChunks = true, particles = false)
	}

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		if (shooter !is PlayerDamager) return //TODO
		val sign = getSign() ?: return

		// Calculate a vector in the direction from the fire point to the targeted block
		val vectorToTarget =
			target.clone().subtract((getFirePos() + pos).toVector()).normalize().multiply(multiblock.range)

		// Add this vector to the fire position to find the position in the direction at max range.
		this.targetedBlock = (getFirePos() + pos).toVector().add(vectorToTarget)
		setFiring(!isFiring, sign, shooter)

		// If it is within range, the raycast will move it forward.
	}

	private fun setFiring(firing: Boolean, sign: Sign, user: PlayerDamager? = null) {
		val alreadyFiring = starship.subsystems.filterIsInstance<MiningLaserSubsystem>().count { it.isFiring }

		when (firing) {
			true -> {
				// Less than but not equal to because it will increase by 1
				if (alreadyFiring < starship.type.maxMiningLasers) {
					isFiring = true
					setUser(sign, user!!.player.name)
					starship.information("Enabled mining laser at $pos")
					startFiringSequence()
				} else {
					starship.information("Your ship can only fire ${starship.type.maxMiningLasers} mining lasers at once!")
					isFiring = false
				}
			}

			false -> {
				isFiring = false
				setUser(sign, null)
				starship.information("Disabled mining laser at $pos")
				starship
				cancelTask()
			}
		}
	}

	private fun startFiringSequence() {
		tick = 0
		val fireTask =
			runnable {
				if (isFiring) {
					fire()
				} else {
					cancel()
				}
			}.runTaskTimer(IonServer, 0L, 5L)

		starship.world.players.forEach {
			if (it.location.distance(
					multiblock.getFirePointOffset().plus(pos).toLocation(starship.world)
				) < multiblock.range * 2
			) {
				it.stopSound(multiblock.sound)

				starship.world.playSound(
					it.location,
					"starship.weapon.mining_laser.start",
					SoundCategory.PLAYERS,
					1.0f,
					1.0f
				)
			}
		}

		firingTasks.add(fireTask)
	}

	private fun cancelTask() {
		isFiring = false
		firingTasks.forEach { it.cancel() }
		firingTasks.clear()

		// Stop sound
		for (player in starship.world.players) {
			if (
				player.location.distance(
					starship.centerOfMass.toLocation(starship.world)
				) > multiblock.range
			) {
				continue
			}

			starship.world.playSound(
				player.location,
				"starship.weapon.mining_laser.stop",
				1.0f,
				1f
			)
		}
	}

	fun fire() {
		val initialPos = getFirePos().toLocation(starship.world).toCenterLocation().add(pos.toVector())
		val targetVector = targetedBlock.clone().subtract(initialPos.toVector())
		// Cancel if
		val sign = getSign() ?: return cancelTask()
		val controller = starship.controller

		if (!ActiveStarships.isActive(starship) || controller == null) {
			setFiring(false, sign)
			return
		}

		val isPlots = starship.world.name.contains("plots", ignoreCase = true)

		if (!SpaceWorlds.contains(starship.world) && !isPlots) {
			starship.sendMessage(
				text("The Mining Laser at ${sign.block.x}, ${sign.block.y}, ${sign.block.z} wasn't able to initialize its gravitational collection beam and was disabled! (Move to a space world)")
			)
			return setFiring(false, sign, null)
		}

		val power = PowerMachines.getPower(sign, true)

		if (power == 0) {
			starship.alert("Mining Laser at ${sign.block.x}, ${sign.block.y}, ${sign.block.z} ran out of power and was disabled!")

			setFiring(false, sign)
			return
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
		CrystalLaser(initialPos, laserEnd, 5, -1).durationInTicks().apply { start(IonServer) }

		val blocks = getBlocksToDestroy(laserEnd.block)

		if (blocks.any { starship.contains(it.x, it.y, it.z) }) {
			starship.sendMessage(
				text(
					"Mining Laser at ${sign.block.x}, ${sign.block.y}, ${sign.block.z} became obstructed and was disabled!"
				).color(NamedTextColor.RED)
			)
			return setFiring(false, sign, null)
		}

		val blocksBroken = DrillMultiblock.breakBlocks(
			sign = sign,
			maxBroken = multiblock.maxBroken,
			toDestroy = blocks,
			output = multiblock.getOutput(sign),
			user = controller
		) {
			val event = StarshipBreakBlockEvent(
				controller,
				it
			).callEvent()

			controller.canDestroyBlock(it) && event
		}

		if (blocksBroken > 0) {
			PowerMachines.setPower(sign, power - (blockBreakPowerUsage * blocksBroken).toInt(), true)

			laserEnd.world.spawnParticle(Particle.EXPLOSION_HUGE, laserEnd, 1)
		}  else {
			starship.sendActionBar(text("Mining laser is trying to break air!", NamedTextColor.RED))
		}

		// Sound is 5 seconds, ticks every quarter second
		if (tick % 20 == 0) {
			val soundOrigin = getFirePos().plus(pos).toLocation(starship.world)

			soundOrigin.world.players.forEach {
				if (it.location.distance(soundOrigin) < multiblock.range * 2) {
					it.stopSound(multiblock.sound)

					soundOrigin.world.playSound(it.location, multiblock.sound, SoundCategory.PLAYERS, 1.0f, 1.0f)
				}
			}

			tick = 0
		}

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
