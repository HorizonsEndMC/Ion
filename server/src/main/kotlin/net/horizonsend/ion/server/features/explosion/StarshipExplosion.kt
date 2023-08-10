package net.horizonsend.ion.server.features.explosion

import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.event.explosion.StarshipCauseExplosionEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ExplosionDamageCalculator
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import java.util.Optional
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Mostly async starship explosion, supporting the controller API
 * @see Controller
 **/
class StarshipExplosion(
	val world: World,
	val x: Double,
	val y: Double,
	val z: Double,
	val power: Float,
	val originator: Controller,
	val blocks: MutableList<Block> = mutableListOf(),
) {
	private val toExplode = mutableSetOf<BlockPos>()
	var useRays = false
	var useFire = false

	val random = Random.Default
	private val damageCalculator = ExplosionDamageCalculator()

	fun explode(applyPhysics: Boolean = true, callback: (StarshipExplosion) -> Unit = {}) {
		getBlocksAsync()
		if (useRays) getRayBlocksAsync()

		val event = StarshipCauseExplosionEvent(
			originator,
			this
		)

		val isCancelled = event.callEvent()

		if (isCancelled) return

		Tasks.sync {
			removeBlocks(applyPhysics)

			if (useFire) applyFire()

			callback(this)
		}
	}

	/** populates the blocks list **/
	private fun getBlocksAsync() {
		val maxRadius = getMaxRadius()

		for (x in 0..maxRadius) for (y in 0..maxRadius) for (z in 0..maxRadius) {
			//TODO
		}
	}

	/** If specified for the explosion to use rays, it additionally populates the blocks list **/
	private fun getRayBlocksAsync() {
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
		// TODO
	}

	/** removes blocks specified in the blocks list **/
	private fun removeBlocks(applyPhysics: Boolean) {
		for (block in blocks) {
			block.setType(Material.AIR, applyPhysics)
		}
	}

	/** Max radius for the crater **/
	private fun getMaxRadius(): Int = sqrt(power).roundToInt()

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
			callback: (StarshipExplosion) -> Unit,
		) : StarshipExplosion {
			val (world, x, y, z) = location

			world!!

			val explosion = StarshipExplosion(
				world,
				x,
				y,
				z,
				power,
				source
			)

			explosion.useFire = useFire

			explosion.useRays = useRays

			explosion.explode(applyPhysics, callback)

			return explosion
		}
	}
}
