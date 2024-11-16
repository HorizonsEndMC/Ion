package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.items.misc.PackagedMultiblock
import net.horizonsend.ion.server.features.multiblock.shape.BlockRequirement
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
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

	fun place(player: Player, origin: Block, direction: BlockFace, multiblock: Multiblock, itemSource: Inventory?) {
		val requirements = multiblock.shape.getRequirementMap(direction)
		val placements = mutableMapOf<Block, BlockData>()

		for ((offset, requirement) in requirements) {
			val absolute = Vec3i(origin.x, origin.y, origin.z) + offset
			val (x, y, z) = absolute

			if (itemSource != null) {
				itemSource
					.filterNotNull()
					.firstOrNull { requirement.itemRequirement.itemCheck(it) }
					?.let {
						requirement.itemRequirement.consume(it)
					}
			}

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

		if (multiblock is SignlessStarshipWeaponMultiblock<*>) return

		// Add sign
		val signPosition = origin.getRelative(direction.oppositeFace, 1)
		val signData = Material.OAK_WALL_SIGN.createBlockData { signData ->
			signData as Directional
			signData.facing = direction.oppositeFace
		}

		signPosition.blockData = signData
		val sign = signPosition.state as Sign

		// Set the detection name just in case the setup fails
		sign.getSide(Side.FRONT).line(0, text("[${multiblock.name}]"))
		sign.update()

		MultiblockAccess.tryDetectMultiblock(player, sign, direction, false)

		multiblock.setupSign(player, sign)
	}

	fun getTokenData(itemStack: ItemStack): Multiblock? {
		val data = itemStack.itemMeta.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.TAG_CONTAINER) ?: return null
		val storageName = data.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING)!!
		return MultiblockRegistration.getByStorageName(storageName)!!
	}

	fun setTokenData(multiblock: Multiblock, destination: PersistentDataContainer) {
		val pdc = destination.adapterContext.newPersistentDataContainer()
		pdc.set(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING, multiblock.javaClass.simpleName)

		destination.set(NamespacedKeys.MULTIBLOCK, PersistentDataType.TAG_CONTAINER, pdc)
	}

	/** Moves the needed materials for the provided multiblock from the list of items to the destination inventory */
	fun packageFrom(items: List<ItemStack>, multiblock: Multiblock, destination: Inventory) {
		val itemRequirements = multiblock.shape.getRequirementMap(BlockFace.NORTH).map { it.value }
		val missing = mutableListOf<BlockRequirement>()

		for (blockRequirement in itemRequirements) {
			val requirement = blockRequirement.itemRequirement
			val success = items.firstOrNull { requirement.itemCheck(it) && requirement.consume(it) }

			if (success == null) {
				missing.add(blockRequirement)
				continue
			}

			val amount = requirement.amountConsumed(success)
			LegacyItemUtils.addToInventory(destination, success.asQuantity(amount))
		}
	}

	fun createPackagedItem(availableItems: List<ItemStack>, multiblock: Multiblock): ItemStack {
		val base = PackagedMultiblock.createFor(multiblock)
		return base.updateMeta {
			it as BlockStateMeta

			@Suppress("UnstableApiUsage")
			val newState = Material.CHEST.createBlockData().createBlockState() as Chest
			packageFrom(availableItems, multiblock, newState.inventory)

			it.blockState = newState
		}
	}

	/**
	 * Returns a list of requirements not met by the existing items
	 **/
	fun checkRequirements(available: Iterable<ItemStack>, multiblock: Multiblock): List<BlockRequirement> {
		val items = available.mapTo(mutableListOf()) { it.clone() }

		val itemRequirements = multiblock.shape.getRequirementMap(BlockFace.NORTH).map { it.value }
		val missing = mutableListOf<BlockRequirement>()

		for (blockRequirement in itemRequirements) {
			val requirement = blockRequirement.itemRequirement
			if (items.any { requirement.itemCheck(it) && requirement.consume(it) }) continue
			missing.add(blockRequirement)
		}

		return missing
	}
}
