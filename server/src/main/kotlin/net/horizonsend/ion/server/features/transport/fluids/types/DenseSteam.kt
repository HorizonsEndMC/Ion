package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid.Companion.windDirection
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork.Companion.PIPE_INTERIOR_PADDING
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.random.Random

object DenseSteam : FluidType(FluidTypeKeys.DENSE_STEAM) {
	override val categories: Array<FluidCategory> = arrayOf()

	val color: Color = Color.WHITE

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

		val destination = start.clone().add(offset).add(leakingDirection.direction.multiply(5)).add(windDirection.multiply(2))

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

	override fun getDisplayName(stack: FluidStack): Component {
		if (stack.hasData(FluidPropertyTypeKeys.TEMPERATURE)) {
			val boiling = stack.getData(FluidPropertyTypeKeys.TEMPERATURE.getValue())?.value?.let { it > 100.0 }
			if (boiling == true) return text("Dry Steam")
		}

		return text("Steam")
	}

	override fun getIsobaricHeatCapacity(stack: FluidStack): Double {
		return 2.030
	}

	override fun getDensity(stack: FluidStack, location: Location?): Double {
		// https://www.spiraxsarco.com/resources-and-design-tools/steam-tables/superheated-steam-region?sc_lang=en-GB
		return 1.13607
	}

	override fun getMolarMass(): Double {
		return 18.01528
	}
}
