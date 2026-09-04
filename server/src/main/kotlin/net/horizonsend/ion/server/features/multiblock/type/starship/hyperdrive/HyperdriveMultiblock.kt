package net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive

import net.horizonsend.ion.server.command.misc.NavigationCommand
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.add
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.BlockInventoryHolder

abstract class HyperdriveMultiblock : Multiblock(), InteractableMultiblock, DisplayNameMultilblock {
	override val name = "hyperdrive"

	abstract val maxPower: Int
	abstract val hyperdriveClass: Int

	override val description: Component get() = text("Allows a starship to enter and exit hyperspace. Consumes chetherite.")

	open val chetheritePerInventory = 2;

	protected abstract fun buildFuelInventoryOffsets(): List<Vec3i>

	private val fuelInventoryOffsets: Map<BlockFace, List<Vec3i>> =
		CARDINAL_BLOCK_FACES.associate { inward ->
			val right = inward.rightFace
			val offsets: List<Vec3i> = buildFuelInventoryOffsets().map { (x, y, z) ->
				Vec3i(x = right.modX * x + inward.modX * z, y = y, z = right.modZ * x + inward.modZ * z)
			}
			return@associate inward to offsets
		}

	open fun addFuelInventories(multiblockShape: MultiblockShape) = buildFuelInventoryOffsets().forEach { (x, y, z) ->
		multiblockShape.at(x, y, z).hopper()
	}

	fun getFuelInventories(sign: Sign): Set<BlockInventoryHolder> {
		val inwards = sign.getFacing().oppositeFace
		val offsets = fuelInventoryOffsets[inwards] ?: error("Unhandled sign direction $inwards")

		val origin = sign.location.add(inwards)

		return offsets.map { origin.clone().add(it).block.state as BlockInventoryHolder }.toSet()
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		super.onTransformSign(player, sign)
		sign.getSide(Side.FRONT).line(3, text("Select Destination", NamedTextColor.RED))
		sign.update()
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val starship = ActiveStarships.findByPilot(player)
		if (starship?.isDirectControlEnabled == true || starship?.isDirectCruiseControlEnable == true) return
		NavigationCommand.openNavigationGui(player)
	}
}
