package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockRegistration
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object PrePackaged {
	fun getOriginFromPlacement(clickedBlock: Block, direction: BlockFace, shape: MultiblockShape): Block {
		val requirements = shape.getRequirementMap(direction)
		val minY = requirements.entries.minOfOrNull { it.key.y } ?: throw NullPointerException("Multiblock has no shape!")

		return clickedBlock
			.getRelative(BlockFace.UP, 1) // Get the block on top of the clicked block
			.getRelative(BlockFace.DOWN, minY) // Go up (down negative blocks) until the origin is high enough to fit the blocks below it
	}

	fun checkObstructions(origin: Block, direction: BlockFace, shape: MultiblockShape): List<Vec3i> {
		val requirements = shape.getRequirementMap(direction)
		val obstructed = mutableListOf<Vec3i>()

		for ((offset, _) in requirements) {
			val offsetBlock = origin.getRelativeIfLoaded(offset.x, offset.y, offset.z)

			if (offsetBlock == null) {
				obstructed.add(Vec3i(origin.x, origin.y, origin.z).plus(offset))
				continue
			}

			if (!offsetBlock.type.isAir) {
				obstructed.add(Vec3i(origin.x, origin.y, origin.z).plus(offset))
			}
		}

		val signPosition = origin.getRelative(direction.oppositeFace, 1)
		if (!signPosition.type.isAir) {
			obstructed.add(Vec3i(signPosition.x, signPosition.y, signPosition.z))
		}

		return obstructed
	}

	fun place(player: Player, origin: Block, direction: BlockFace, data: PackagedMultiblockData) {
		val requirements = data.multiblock.shape.getRequirementMap(direction)
		val placements = mutableMapOf<Block, BlockData>()

		for ((offset, requirement) in requirements) {
			val absolute = Vec3i(origin.x, origin.y, origin.z) + offset
			val (x, y, z) = absolute

			val existingBlock = origin.world.getBlockAt(x, y, z)

			val event = BlockPlaceEvent(
				existingBlock,
				existingBlock.state,
				existingBlock,
				player.activeItem,
				player,
				true,
				EquipmentSlot.HAND
			).callEvent()

			if (!event) return player.userError("You can't build here!")

			placements[existingBlock] = requirement.example.invoke(direction)
		}

		for ((block, placement) in placements) {
			block.blockData = placement
			val soundGroup = placement.soundGroup
			origin.world.playSound(block.location, soundGroup.placeSound, soundGroup.volume, soundGroup.pitch)
		}

		// Add sign
		val signPosition = origin.getRelative(direction.oppositeFace, 1)
		val signData = Material.OAK_WALL_SIGN.createBlockData { signData ->
			signData as Directional
			signData.facing = direction.oppositeFace
		}

		signPosition.blockData = signData
		val sign = signPosition.state as Sign

		// Set the detection name just in case the setup fails
		sign.getSide(Side.FRONT).line(0, text("[${data.multiblock.name}]"))
		sign.update()

		MultiblockAccess.tryDetectMultiblock(player, sign, direction, false)

		data.multiblock.setupSign(player, sign)
	}

	data class PackagedMultiblockData(
		val multiblock: Multiblock
	)

	fun getPackagedData(itemStack: ItemStack): PackagedMultiblockData? {
		val data = itemStack.itemMeta.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.TAG_CONTAINER) ?: return null
		//TODO rework for player packaged items
		val storageName = data.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING)!!
		val multiblock = MultiblockRegistration.getByStorageName(storageName)!!

		return PackagedMultiblockData(multiblock)
	}

	fun packageData(data: PackagedMultiblockData, destination: PersistentDataContainer) {
		val pdc = destination.adapterContext.newPersistentDataContainer()
		pdc.set(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING, data.multiblock.javaClass.simpleName)

		destination.set(NamespacedKeys.MULTIBLOCK, PersistentDataType.TAG_CONTAINER, pdc)
	}
}
