package net.horizonsend.ion.server.features.starship.subsystem.misc.tug

import io.papermc.paper.raytracing.RayTraceTarget
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.type.starship.misc.TugMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.cube
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distance
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.roundToInt

class TugSubsystem(starship: Starship, pos: Vec3i, override var face: BlockFace, val multiblock: TugMultiblock) : WeaponSubsystem(starship, pos), DirectionalSubsystem, ManualWeaponSubsystem {
	override val balancing = ConfigurationFiles.starshipBalancing().platformBalancing.weapons.aiHeavyLaser

	override val powerUsage: Int = 0

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !isFloodFilling
	}

	override fun getName(): Component {
		return Component.text("Tug")
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return target.clone().subtract(getFirePosition().toVector()).normalize()
	}

	private fun getFirePosition(): Location {
		val firePos = getRelative(pos.getRelative(face.oppositeFace), face.oppositeFace, multiblock.firePosOffset.x, multiblock.firePosOffset.y, multiblock.firePosOffset.z)
		return firePos.toCenterVector().toLocation(starship.world)
	}

	override fun isIntact(): Boolean {
		val origin = pos.getRelative(face.oppositeFace)
		starship.highlightBlock(origin, 10L)
		return multiblock.blockMatchesStructure(starship.world.getBlockAt(origin.x, origin.y, origin.z), face.oppositeFace)
	}

	fun setControlMode(new: TugControlMode) {
		controlMode.onStop(starship)
		controlMode = new
		new.onSetup(starship)
	}

	private var controlMode: TugControlMode = TugControlMode.FOLLOW

	var minPoint: Vec3i? = null
	var maxPoint: Vec3i? = null
	val centerPoint: Vec3i? get() = minPoint?.let { min -> maxPoint?.let { max -> Vec3i((min.x + max.x) / 2, (min.y + max.y) / 2, (min.z + max.z) / 2) } }

	var movedBlocks = LongArray(0)

	var isFloodFilling: Boolean = false

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		if (shooter !is PlayerDamager) return

		val originLoc = getFirePosition()

		val distance = distance(target, originLoc.toVector())
		val beamDistance = minOf(100, distance.roundToInt())

		originLoc
			.alongVector(dir.clone().multiply(beamDistance), beamDistance)
			.forEach { intermediate ->
				starship.world.spawnParticle(Particle.SOUL_FIRE_FLAME, intermediate.x, intermediate.y, intermediate.z, 0, 0.0, 0.0, 0.0, 0.0, null, true)
			}

		val hitBlock = starship.world.rayTrace {
			it.direction(dir)
			it.start(originLoc)
			it.blockFilter { verifyBlock(shooter.player, it) }
			it.maxDistance(100.0)
			it.targets(RayTraceTarget.BLOCK)
		}?.hitBlock ?: return

		try {
			isFloodFilling = true
			setupMovedBlocks(shooter.player, Vec3i(hitBlock.location)) {
				isFloodFilling = false
			}
		} catch (e: Throwable) {
			isFloodFilling = false
			throw e
		}
	}

	fun setupMovedBlocks(player: Player, pos: Vec3i, callback: () -> Unit = {}) = Tasks.async {
		val standingOn = toBlockKey(pos)
		val world = starship.world

		val visited = LongOpenHashSet.of(standingOn)
		val visitQueue = ArrayDeque<Long>(listOf(standingOn))

		val foundBlocks = LongOpenHashSet()

		var minX: Int = Int.MAX_VALUE
		var minY: Int = Int.MAX_VALUE
		var minZ: Int = Int.MAX_VALUE

		var maxX: Int = Int.MIN_VALUE
		var maxY: Int = Int.MIN_VALUE
		var maxZ: Int = Int.MIN_VALUE

		while (visitQueue.isNotEmpty()) {
			val current = visitQueue.removeFirst()
			val x = getX(current)
			val y = getY(current)
			val z = getZ(current)

			val block = getBlockIfLoaded(world, x, y, z)

			if (block == null) {
				starship.userError("That structure goes beyond loaded chunks!")
				return@async
			}

			if (!verifyBlock(player, block)) continue

			foundBlocks.add(blockKey(x, y, z))

			if (foundBlocks.size > MAX_ASTEROID_SIZE) {
				starship.userError("That structure is too large to move!")
				return@async
			}

			if (minX > x) minX = x
			if (minY > y) minY = y
			if (minZ > z) minZ = z
			if (maxX < x) maxX = x
			if (maxY < y) maxY = y
			if (maxZ < z) maxZ = z

			// Detect adjacent blocks
			for (offsetX in -1..1) {
				for (offsetY in -1..1) {
					for (offsetZ in -1..1) {
						val adjacentX = offsetX + x
						val adjacentY = offsetY + y
						val adjacentZ = offsetZ + z

						// Ensure it's a valid Y-level before adding it to the queue
						if (adjacentY < 0 || adjacentY > world.maxHeight) {
							continue
						}

						val adjacent = toBlockKey(adjacentX, adjacentY, adjacentZ)
						if (visited.add(adjacent)) {
							visitQueue.addFirst(adjacent)
						}
					}
				}
			}
		}

		starship.information("Acquired ${foundBlocks.size} blocks")
		movedBlocks = foundBlocks.toLongArray()

		callback.invoke()

		if (movedBlocks.isEmpty()) return@async

		this.minPoint = Vec3i(minX, minY, minZ)
		this.maxPoint = Vec3i(maxX, maxY, maxZ)
	}

	override fun onMovement(movement: TransformationAccessor, success: Boolean) {
		if (movement !is TranslateMovement || !success || controlMode != TugControlMode.FOLLOW) return
		doMovement(movement)
	}

	var lastTaskFuture: Future<Boolean>? = null

	fun doMovement(movement: TransformationAccessor) {
		var lastTicked = System.currentTimeMillis()

		if (lastTaskFuture?.isDone == false) {
			return
		}

		val future = CompletableFuture<Boolean>()
		lastTaskFuture = future

		Tasks.async {
			try {
				starship.information("Moving ${movedBlocks.size} blocks")
				movement.execute(
					positions = movedBlocks,
					world1 = starship.world,
					executionCheck = { true }
				) {
					movedBlocks = it

					minPoint?.let { minPoint = movement.displaceVec3i(it) }
					maxPoint?.let { maxPoint = movement.displaceVec3i(it) }
				}

				val now = System.currentTimeMillis()
				starship.information("Movement took ${(now - lastTicked) / 1000.0}s")
				lastTicked = now
				future.complete(true)
			} catch (e: StarshipMovementException) {
				starship.sendMessage(ofChildren(Component.text("Towed Load Blocked! ", NamedTextColor.RED), e.formatMessage()))
				future.complete(false)
			}
		}
	}

	override fun tick() {
		val minVec = minPoint ?: return
		val maxVec = maxPoint?.plus(Vec3i(1, 1, 1)) ?: return

		cube(
            minVec.toLocation(starship.world),
            maxVec.toLocation(starship.world)
        ).forEach { t ->
			starship.playerPilot?.spawnParticle(Particle.ELECTRIC_SPARK, t.x, t.y, t.z, 1, 0.0, 0.0, 0.0, 0.0, null, true)
		}
	}

	fun verifyBlock(player: Player, block: Block): Boolean {
		val type = block.getTypeSafe() ?: return false
		if (type.isAir) return false

		if (starship.contains(block.x, block.y, block.z)) return false

		if (block.world.ion.detectionForbiddenBlocks.contains(toBlockKey(block.x, block.y, block.z))) return false

		if (ActiveStarships.findByBlock(block) != null) {
			return false
		}

		return !ProtectionListener.denyBlockAccess(player, block.location, null)
	}

	companion object {
		const val MAX_ASTEROID_SIZE = 1_000_000
	}
}
