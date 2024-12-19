package net.horizonsend.ion.server.features.multiblock.type.hyperdrive

import net.horizonsend.ion.server.features.gui.custom.navigation.NavigationSystemMapGui
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.add
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.BlockFace
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

abstract class HyperdriveMultiblock : Multiblock(), InteractableMultiblock {
	override val name = "hyperdrive"

	abstract val maxPower: Int
	abstract val hyperdriveClass: Int

	protected abstract fun buildHopperOffsets(): List<Vec3i>

	private val hopperOffsets: Map<BlockFace, List<Vec3i>> =
		CARDINAL_BLOCK_FACES.associate { inward ->
			val right = inward.rightFace
			val offsets: List<Vec3i> = buildHopperOffsets().map { (x, y, z) ->
				Vec3i(x = right.modX * x + inward.modX * z, y = y, z = right.modZ * x + inward.modZ * z)
			}
			return@associate inward to offsets
		}

	protected fun addHoppers(multiblockShape: MultiblockShape) = buildHopperOffsets().forEach { (x, y, z) ->
		multiblockShape.at(x, y, z).hopper()
	}

	fun getHoppers(sign: Sign): Set<Hopper> {
		val inwards = sign.getFacing().oppositeFace
		val offsets = hopperOffsets[inwards] ?: error("Unhandled sign direction $inwards")

		val origin = sign.location.add(inwards)

		return offsets.map { origin.clone().add(it).block.getState(false) as Hopper }.toSet()
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		super.onTransformSign(player, sign)
		sign.getSide(Side.FRONT).line(3, text("Select Destination", NamedTextColor.RED))
		sign.update()
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		NavigationSystemMapGui(player, player.world).openMainWindow()
	}
}
