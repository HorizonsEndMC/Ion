package net.horizonsend.ion.server.features.explosion

import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.event.explosion.StarshipCauseExplosionEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.toBlockPos
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.Optional
import java.util.concurrent.CompletableFuture
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.asJavaRandom

/**
 * Mostly async starship explosion, supporting the controller API
 * @see Controller
 **/
class Explosion(
	val world: World,
	val x: Double,
	val y: Double,
	val z: Double,
	val power: Float,
	val controller: Controller
) {
	private var fireType = Material.FIRE
	private val toExplode = mutableSetOf<BlockPos>()
	var useRays = false
	var useFire = false

	val random = Random.asJavaRandom()
	val noise = SimplexOctaveGenerator(random, 3)
	val blocks: MutableList<Block> = mutableListOf()

	fun explode(applyPhysics: Boolean = true, callback: (Explosion) -> Unit = {}) {
		println(5)
		if (useRays) getRayBlockPositionsAsync() else getBlockPositionsAsync()
		println(6)

		val event = StarshipCauseExplosionEvent(
			controller,
			this
		)

		val isCancelled = event.callEvent()
		println(7)

		if (!isCancelled) return
		println(8)

		Tasks.sync {
			populateBlocks()
			println(9)
			removeBlocks(applyPhysics)
			println(10)

			if (useFire) applyFire()

			callback(this)
		}
	}

	/** populates the blocks list **/
	private fun getBlockPositionsAsync() {
		val collected = mutableSetOf<BlockPos>()

		Tasks.async {
			val maxRadius = getMaxRadius()
			println("getting blocks 1")

			val coveredChunks = mutableSetOf<CompletableFuture<Chunk>>()
			println("getting blocks 2")

			val originX = this.x.toInt()
			val originY = this.y.toInt()
			val originZ = this.z.toInt()

			val xRange = IntRange(-maxRadius, maxRadius).associateWith { it + originX }
			val yRange = IntRange(-maxRadius, maxRadius).associateWith { it + originY }
			val zRange = IntRange(-maxRadius, maxRadius).associateWith { it + originZ }
			println("getting blocks 3")

			for ((_, absoluteX) in xRange) for ((_, absoluteZ) in zRange) {
				val chunkX = absoluteX.shr(4)
				val chunkZ = absoluteZ.shr(4)

				coveredChunks += world.getChunkAtAsync(chunkX, chunkZ)
			}

			val completedChunks = mutableMapOf<ChunkPos, Chunk>()

			// Get the chunks async
			CompletableFuture.allOf(*coveredChunks.toTypedArray()).thenAccept {
				coveredChunks.associateTo(completedChunks) {
					val chunk = it.get()

					return@associateTo ChunkPos(chunk.x, chunk.z) to chunk
				}
			}

			for ((localX, absoluteX) in xRange) {
				val xSquared = localX * localX
				val absoluteXDouble = absoluteX.toDouble()

				for ((localY, absoluteY) in yRange) {
					val ySquared = localY * localY
					val absoluteYDouble = absoluteY.toDouble()

					for ((localZ, absoluteZ) in zRange) {
						val zSquared = localZ * localZ
						val absoluteZDouble = absoluteZ.toDouble()

						val radius = noise.noise(absoluteXDouble, absoluteYDouble, absoluteZDouble, true) * (maxRadius / 4)

						if ((xSquared + ySquared + zSquared) >= maxRadius + radius) continue

						toExplode.add(BlockPos(absoluteX, absoluteY, absoluteZ))
					}
				}
			}
		}

		toExplode.addAll(collected)
	}

	fun populateBlocks() {
		for (block in toExplode) {
			val (x, y, z) = block

			blocks.add(getBlockIfLoaded(world, x, y, z) ?: continue)
		}
	}

	/** If specified for the explosion to use rays, it additionally populates the blocks list **/
	private fun getRayBlockPositionsAsync() {
		val positions = mutableSetOf<BlockPos>()

		val radius = power.toDouble().coerceAtLeast(0.0).toFloat()

		// I'm not even gonna try to understand how this works
		for (x in 0 until 16) for (y in 0 until 16) for (z in 0 until 16) {
			if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
				var d0: Double = (x.toFloat() / 15.0f * 2.0f - 1.0f).toDouble()
				var d1: Double = (y.toFloat() / 15.0f * 2.0f - 1.0f).toDouble()
				var d2: Double = (z.toFloat() / 15.0f * 2.0f - 1.0f).toDouble()

				val d3 = sqrt(d0 * d0 + d1 * d1 + d2 * d2)

				d0 /= d3
				d1 /= d3
				d2 /= d3

				var f: Float = radius * (0.7f + random.nextFloat() * 0.6f)

				var d4 = this.x
				var d5 = this.y
				var d6 = this.z

				while (f > 0.0f) {
					val blockPos = BlockPos.containing(d4, d5, d6)
					val blockState: BlockState = getNMSBlockDataSafe(world, blockPos) ?: continue

					if (!blockState.isDestroyable) {
						f -= 0.22500001f
						continue
					}

					val fluid = blockState.fluidState

					if (!this.world.minecraft.isInWorldBounds(blockPos)) break

					val optional: Optional<Float> = getBlockExplosionResistance(blockState, fluid)

					if (optional.isPresent) f -= (optional.get() + 0.3f) * 0.3f

					if (f > 0.0f) { positions.add(blockPos) }

					d4 += d0 * 0.30000001192092896
					d5 += d1 * 0.30000001192092896
					d6 += d2 * 0.30000001192092896
					f -= 0.22500001f
				}
			}
		}

		toExplode.addAll(positions)
	}

	/** Applies fire to the explosion after blocks have been removed **/
	private fun applyFire() {
		val supported = CompletableFuture<Set<BlockPos>>()

		Tasks.async {
			val supportedBlocks = mutableSetOf<BlockPos>()

			for (block in blocks) {
				val relative = block.getRelativeIfLoaded(BlockFace.DOWN) ?: continue

				if (relative.type.isAir) continue

				supportedBlocks.add(relative.location.toBlockPos())
			}

			supported.complete(supportedBlocks)
		}

		supported.thenAccept {
			Tasks.sync {
				for (pos in it) {
					val (x, y, z) = pos

					world.setType(x, y, z, fireType)
				}
			}
		}
	}

	/** removes blocks specified in the blocks list **/
	private fun removeBlocks(applyPhysics: Boolean) = Tasks.sync {
		println(12)
		for (block in blocks) {
			println(block)
			block.setType(Material.AIR, applyPhysics)
		}
	}

	/** Max radius for the crater **/
	private fun getMaxRadius(): Int = sqrt(power).roundToInt() * 2

	private fun getBlockExplosionResistance(
		blockState: BlockState,
		fluidState: FluidState,
	): Optional<Float> {
		return if (blockState.isAir && fluidState.isEmpty) Optional.empty() else Optional.of(
			blockState.block.explosionResistance.coerceAtLeast(fluidState.explosionResistance)
		)
	}

	companion object {
		fun World.explode(
			location: Location,
			power: Float, source: Controller,
			useFire: Boolean = false,
			useRays:
			Boolean = false,
			applyPhysics: Boolean = true,
			fireType: Material = Material.FIRE,
			callback: (Explosion) -> Unit = {},
		) : Explosion {
			println(1)
			val (_, x, y, z) = location
			println(2)

			val explosion = Explosion(
				this,
				x,
				y,
				z,
				power,
				source
			)

			explosion.fireType = fireType

			explosion.useFire = useFire

			explosion.useRays = useRays

			println(3)
			explosion.explode(applyPhysics, callback)

			return explosion
		}
	}
}
