package net.horizonsend.ion.server.features.explosion

import it.unimi.dsi.fastutil.objects.ObjectSets
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.event.explosion.StarshipCauseExplosionEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.toBlockPos
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Source.MASTER
import net.kyori.adventure.sound.Sound.sound
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.Optional
import java.util.concurrent.CompletableFuture
import kotlin.math.max
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
	private val toExplode = ObjectSets.emptySet<Vec3i>()

	var useFire = false
	var firePercent = 0.05

	val random = Random.asJavaRandom()
	val blocks: MutableSet<Block> = ObjectSets.emptySet()

	private var particle: Particle? = null
	private var sound: Sound? = null

	fun explode(applyPhysics: Boolean = true, callback: (Explosion) -> Unit = {}) = Tasks.async {
		val blockPositions = getRayBlockPositionsAsync()

		blockPositions.thenAccept {
			toExplode.addAll(it)

			populateBlocks()

			val event = StarshipCauseExplosionEvent(
				controller,
				this@Explosion,
				blocks,
			)

			val isCancelled = event.callEvent()

			if (!isCancelled) return@thenAccept

			Tasks.sync {
				removeBlocks(applyPhysics)

				if (useFire) applyFire()

				particle?.let { particle ->
					val particles = max(1, sqrt(power).toInt())
					val offset = sqrt(power).toDouble()

					world.spawnParticle(particle, x, y ,z, particles, offset, offset, offset)
				}

				sound?.let { sound ->
					world.playSound(sound, x, y, z)
				}

				callback(this)
			}
		}
	}

	/** Get the blocks in the positions specified **/
	private fun populateBlocks() {
		for (block in toExplode) {
			val (x, y, z) = block

			// Thread safe
			blocks.add(getBlockIfLoaded(world, x, y, z) ?: continue)
		}
	}

	/** If specified for the explosion to use rays, it additionally populates the blocks list **/
	// This is the NMS explosion code
	private fun getRayBlockPositionsAsync(): CompletableFuture<Set<Vec3i>> {
		val complete = CompletableFuture<Set<Vec3i>>()

		Tasks.async {
			val positions = mutableSetOf<Vec3i>()

			val radius = power.toDouble().coerceAtLeast(0.0).toFloat()

			// TODO Variable names
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

						if (f > 0.0f) positions.add(Vec3i(blockPos))

						d4 += d0 * 0.30000001192092896
						d5 += d1 * 0.30000001192092896
						d6 += d2 * 0.30000001192092896
						f -= 0.22500001f
					}
				}
			}

			complete.complete(positions)
		}

		return complete
	}

	/** Applies fire to the explosion after blocks have been removed **/
	private fun applyFire() {
		val supported = CompletableFuture<Set<BlockPos>>()

		Tasks.async {
			val random = Random(world.seed)

			val supportedBlocks = mutableSetOf<BlockPos>()

			for (block in blocks) {
				val relative = block.getRelativeIfLoaded(BlockFace.DOWN) ?: continue

				if (relative.type.isAir) continue

				if (random.nextDouble() <= 1.0 - firePercent) continue

				supportedBlocks.add(block.location.toBlockPos())
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
		for (block in blocks) {
			block.setType(Material.AIR, applyPhysics)
		}
	}

	private fun getBlockExplosionResistance(
		blockState: BlockState,
		fluidState: FluidState,
	): Optional<Float> {
		return if (blockState.isAir && fluidState.isEmpty) Optional.empty() else Optional.of(
			blockState.block.explosionResistance.coerceAtLeast(fluidState.explosionResistance)
		)
	}

	fun location() = Location(world, x, y, z)

	companion object {
		/**
		 * @param modification Modify any additional explosion params using this function
		 *
		 *
		 *
		 **/
		fun World.explode(
			location: Location,
			power: Float, source: Controller,
			modification: (Explosion) -> Unit = {},
			useFire: Boolean = false,
			applyPhysics: Boolean = true,
			fireType: Material = Material.FIRE,
			particle: Particle = Particle.EXPLOSION_HUGE,
			sound: Sound? = sound(key("entity.generic.explode"), MASTER, 1.0f, 1.0f),
			callback: (Explosion) -> Unit = {},
		) : Explosion {
			val (_, x, y, z) = location

			val explosion = Explosion(
				this,
				x,
				y,
				z,
				power,
				source
			)

			modification(explosion)

			explosion.fireType = fireType

			explosion.useFire = useFire

			explosion.particle = particle

			explosion.sound = sound

			explosion.explode(applyPhysics, callback)

			return explosion
		}
	}
}
