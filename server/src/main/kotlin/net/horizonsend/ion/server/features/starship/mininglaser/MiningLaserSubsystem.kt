package net.horizonsend.ion.server.features.starship.mininglaser

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.features.starship.mininglaser.multiblock.MiningLaserMultiblock
import net.horizonsend.ion.server.miscellaneous.extensions.information
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.drills.DrillMultiblock
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.leftFace
import net.starlegacy.util.rightFace
import org.bukkit.Bukkit.getPlayer
import org.bukkit.ChatColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitRunnable
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

	override val powerUsage: Int = 0
	private val blockBreakPowerUsage: Int = 5
	// override fun getAdjustedDir(dir: Vector, target: Vector?): Vector = target?.subtract(getFirePos().toVector()) ?: dir

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
// 		if (target != null) {
// 			val origin = getFirePos().toCenterVector()
// 			val adjustedDir = target.clone().subtract(origin)
//
// 			val horizontalAxis = adjustedDir.clone()
// 			horizontalAxis.y = 0.0
// 			horizontalAxis.rotateAroundY(90.0)
// 			horizontalAxis.normalize()
//
// 			return adjustedDir.normalize()
// 		}

		return target
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
			y = (multiblock.upDownFace().direction.y * y).toInt(),
			z = (right.modZ * x) + (facing.modZ * z)
		)
	}

	private fun getSign() = starship.serverLevel.world.getBlockAt(pos.x, pos.y, pos.z).getState(false) as? Sign

	override fun isIntact(): Boolean {
		val sign = getSign() ?: return false
		return multiblock.signMatchesStructure(sign, loadChunks = true, particles = false)
	}

	private fun getPoints(axis: Vector): List<Location> {
		val spread: Double = 360.0 / multiblock.beamCount
		val points = mutableListOf<Location>()
		val start = axis.clone().normalize().rotateAroundZ(90.0).multiply(multiblock.circleRadius).add(axis.clone())

		for (count in multiblock.beamCount.downTo(1)) {
			val newLoc = start.rotateAroundNonUnitAxis(axis.clone(), spread * count)
			points.add(newLoc.toLocation(starship.serverLevel.world))
		}

		return points
	}

	override fun manualFire(shooter: Player, dir: Vector, target: Vector) {
		if (!isFiring) {
			startFiringSequence()
			this.targetedBlock = target.clone()
			shooter.information(target.toString())
			shooter.information("Enabled mining laser")
		} else {
			firingTasks.forEach { it.cancel() }
			firingTasks.clear()
			shooter.information("Disabled mining laser")
		}

		isFiring = !isFiring
	}

	private fun startFiringSequence() {
		val fireTask = object : BukkitRunnable() {
			override fun run() {
				if (isFiring) {
					fire()
				} else {
					cancel()
				}
			}
		}.runTaskTimer(Ion, 0L, 5L)

		firingTasks.add(fireTask)
	}

	fun fire() {
		if (!ActiveStarships.isActive(starship)) {
			firingTasks.forEach { it.cancel() }
			return
		}

		val sign = getSign() ?: return
		val power = PowerMachines.getPower(sign, true)

		if (power == 0) {
			starship.passengerIDs.forEach {
				getPlayer(it)?.sendMessage(
					String.format(
						"%sDrill at %s ran out of power!",
						ChatColor.RED, sign.location.toVector()
					)
				)

				return
			}
		}

		val intialPos = getFirePos().toLocation(starship.serverLevel.world).toCenterLocation().add(pos.toVector())
		val targetVector = targetedBlock.clone().subtract(intialPos.toVector())

		// TODO rewrite all of the aiming stuff

		val raytrace = starship.serverLevel.world.rayTrace(
			intialPos,
			targetVector.clone(),
			multiblock.range,
			FluidCollisionMode.NEVER,
			true,
			0.1,
			null
		)

		val laserEnd = raytrace?.hitBlock?.location ?: targetedBlock.toLocation(starship.serverLevel.world)
		val laser = CrystalLaser(intialPos, laserEnd, 5, -1).durationInTicks()
		laser.start(Ion)

		val blocks = ArrayList<Block>()
		val block = laserEnd.block

		for (x in -2..2) {
			for (y in -2..2) {
				for (z in -2..2) {
					val toExplode = block.getRelative(BlockFace.EAST, x)
						.getRelative(BlockFace.UP, y)
						.getRelative(BlockFace.SOUTH, z)

					if (toExplode.type != Material.AIR) {
						blocks.add(toExplode)
					}
				}
			}
		}

		if (
			DrillMultiblock.breakBlocks(
				sign = sign,
				maxBroken = 999,
				toDestroy = blocks,
				output = getOutput(sign),
				isDrillMultiblock = false,
				people = starship.passengerIDs.mapNotNull(::getPlayer).toTypedArray(),
				player = starship.pilot!!
			) > 0
		) {
			PowerMachines.setPower(sign, power - blockBreakPowerUsage * 100, true)
		}
	}
}

fun getOutput(sign: Sign): Inventory {
	val direction = sign.getFacing().oppositeFace
	return (
		sign.block.getRelative(direction)
			.getRelative(BlockFace.DOWN)
			.getRelative(direction.leftFace)
			.getState(false) as InventoryHolder
		).inventory
}
