package net.horizonsend.ion.server.features.starship.subsystem.misc

import io.papermc.paper.raytracing.RayTraceTarget
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.type.starship.misc.TugMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
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
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.roundToInt

class TugSubsystem(starship: Starship, pos: Vec3i, override var face: BlockFace, val multiblock: TugMultiblock) : WeaponSubsystem(starship, pos), DirectionalSubsystem, ManualWeaponSubsystem {
	override val balancing = ConfigurationFiles.starshipBalancing().platformBalancing.weapons.aiHeavyLaser

	override val powerUsage: Int = 0

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return true
	}

	override fun getName(): Component {
		return Component.text("Tub")
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return dir
	}


	override fun isIntact(): Boolean {
		val origin = pos.getRelative(face.oppositeFace)
		starship.highlightBlock(origin, 10L)
		return multiblock.blockMatchesStructure(starship.world.getBlockAt(origin.x, origin.y, origin.z), face.oppositeFace)
	}

	var minPoint: Vec3i? = null
	var maxPoint: Vec3i? = null

	var movedBlocks = LongArray(0)

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		if (shooter !is PlayerDamager) return

		val firePos = getRelative(pos.getRelative(face.oppositeFace), face.oppositeFace, multiblock.firePosOffset.x, multiblock.firePosOffset.y, multiblock.firePosOffset.z)

		val originLoc = firePos.toCenterVector().toLocation(starship.world)

		val distance = distance(target, originLoc.toVector())

		originLoc.alongVector(dir.clone().multiply(distance), distance.roundToInt() * 3).forEach { t ->
			starship.world.spawnParticle(Particle.SOUL_FIRE_FLAME, t.x, t.y, t.z, 1, 0.0, 0.0, 0.0, 0.0, null, true)
		}

		val hitBlock = starship.world.rayTrace {
			it.direction(dir)
			it.start(originLoc)
			it.blockFilter { !starship.contains(it.x, it.y, it.z) }
			it.maxDistance(100.0)
			it.targets(RayTraceTarget.BLOCK)
		}?.hitBlock ?: return

		setupMovedBlocks(Vec3i(hitBlock.location))
	}

	fun setupMovedBlocks(pos: Vec3i) = Tasks.async {
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

			val blockData = getBlockIfLoaded(world, x, y, z)

			if (blockData == null) {
				starship.userError("That structure is too large to move!")
				return@async
			}

			if (blockData.type.isAir) continue

			foundBlocks.add(blockKey(x, y, z))

			if (foundBlocks.size > MAX_ASTEROID_SIZE) {
				starship.userError("That structure is too large to move!")
				return@async
			}

			if (minX == null || minX > x) minX = x
			if (minY == null || minY > y) minY = y
			if (minZ == null || minZ > z) minZ = z
			if (maxX == null || maxX < x) maxX = x
			if (maxY == null || maxY < y) maxY = y
			if (maxZ == null || maxZ < z) maxZ = z

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

		if (movedBlocks.isEmpty()) return@async

		this.minPoint = Vec3i(minX, minY, minZ)
		this.maxPoint = Vec3i(maxX, maxY, maxZ)
	}

	override fun onMovement(movement: TransformationAccessor, success: Boolean) {
		if (movement !is TranslateMovement || !success) return
		handleMovement(movement)
	}

	var lastTaskFuture: Future<Boolean>? = null

	fun handleMovement(movement: TranslateMovement) {
		var lastTicked = System.currentTimeMillis()

		if (lastTaskFuture?.get() == false) {
			return
		}

		val future = CompletableFuture<Boolean>()
		lastTaskFuture = future

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
			starship.information("e: $e")
			future.complete(false)
		}
	}

	override fun tick() {
		val minVec = minPoint ?: return
		val maxVec = maxPoint ?: return

		cube(minVec.toCenterVector().toLocation(starship.world), maxVec.toCenterVector().toLocation(starship.world)).forEach { t ->
			starship.playerPilot?.spawnParticle(Particle.SOUL_FIRE_FLAME, t.x, t.y, t.z, 1, 0.0, 0.0, 0.0, 0.0, null, true)
		}
	}

	companion object {
		const val MAX_ASTEROID_SIZE = 1_000_000
	}
}
