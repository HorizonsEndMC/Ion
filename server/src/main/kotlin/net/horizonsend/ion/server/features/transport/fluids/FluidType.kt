package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

abstract class FluidType(override val key: IonRegistryKey<FluidType, out FluidType>) : Keyed<FluidType> {
	abstract val displayName: Component
	abstract val categories: Array<FluidCategory>

	/**
	 * Effect played inside a pipe, when this fluid is present
	 **/
	abstract fun displayInPipe(world: World, origin: Vector, destination: Vector)

	/**
	 * Effect played when leaking form a pipe, when this fluid is leaking
	 **/
	abstract fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace)
}
