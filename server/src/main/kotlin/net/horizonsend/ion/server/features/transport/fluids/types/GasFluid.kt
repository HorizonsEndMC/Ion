package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils.GAS_CONSTANT
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork.Companion.PIPE_INTERIOR_PADDING
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.celsiusToKelvin
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.random.Random

abstract class GasFluid(
	key: IonRegistryKey<FluidType, out FluidType>,
	val color: Color,
	private val heatCapacity: Double,
	private val molarMass: Double,
	val pressureBars: Double = 1.0
) : FluidType(key) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS)

	override fun displayInPipe(world: World, origin: Vector, destination: Vector) {
		val trailOptions = Trail(
			/* target = */ destination.toLocation(world),
			/* color = */ color,
			/* duration = */ 20
		)

		world.spawnParticle(Particle.TRAIL, origin.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0, trailOptions, false)
	}

	override fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace) {
		val openLocation = getRelative(leakingNode.location, leakingDirection)

		val smokeLocation = toVec3i(openLocation).toCenterVector().toLocation(world).add(leakingDirection.direction.multiply(-0.5))

		val offset = Vector(
			Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING),
			Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING),
			Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING)
		)

		val start = smokeLocation.clone().add(offset)

		val destination = start.clone().add(offset).add(leakingDirection.direction.multiply(5)).add(world.ion.enviornmentManager.weatherManager.getWindVector(world, smokeLocation.x, smokeLocation.y, smokeLocation.z))

		val trial = Trail(
			/* target = */ destination,
			/* color = */ color,
			/* duration = */ 40
		)

		world.spawnParticle(Particle.TRAIL, start, 1, 0.0, 0.0, 0.0, 2.125, trial, true)
	}

	override fun onLeak(world: World, location: Vec3i, amount: Double) {
		if (key != FluidTypeKeys.FLUORINE) return //TODO TEMP should be replaced with properties

		val leakingBlock = getBlockIfLoaded(world, location.x, location.y, location.z) ?: return
		if (!leakingBlock.type.isAir) return

//		Tasks.syncBlocking {
//			world.spawn<AreaEffectCloud>(location.toCenterVector().toLocation(world), AreaEffectCloud::class.java) { cloud ->
//				cloud.addCustomEffect(PotionEffect(PotionEffectType.POISON, 3, 3), true)
//				cloud.duration = 3
//				cloud.color = color
//				cloud.radius = 0.5f
//				cloud.height
//				cloud.basePotionType = PotionType.STRONG_POISON
//			}
//		}
	}

	companion object {
		//TODO
		val windDirection: Vector get() = Bukkit.getPlayer("GutinGongoozler")?.location?.direction ?: Vector.getRandom()
	}

	override fun getIsobaricHeatCapacity(stack: FluidStack): Double {
		return heatCapacity * stack.amount
	}

	override fun getMolarMass(): Double {
		return molarMass
	}

	override fun getDensity(stack: FluidStack, location: Location?): Double {
		val temperatureCelsius = stack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE.getValue(), location).value

		val density = (getMolarMass() * pressureBars) / (GAS_CONSTANT * celsiusToKelvin(temperatureCelsius))

		return density
	}
}
