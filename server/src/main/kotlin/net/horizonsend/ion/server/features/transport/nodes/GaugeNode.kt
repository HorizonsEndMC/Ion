package net.horizonsend.ion.server.features.transport.nodes

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.GaugeCustomBlock
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World

interface GaugeNode {
	/**
	 * Sets the redstone output of this gauge, returns whether the signal could be set.
	 **/
	fun setOutput(value: Int): Boolean

	interface CommandBlockGaugeNode : GaugeNode {
		val customBlock: IonRegistryKey<CustomBlock, out CustomBlock>

		fun getGlobalCoordinate(): Vec3i
		fun getWorld(): World

		override fun setOutput(value: Int): Boolean {
			return (customBlock.getValue() as GaugeCustomBlock).setSignalOutput(value, getWorld(), getGlobalCoordinate())
		}
	}
}
