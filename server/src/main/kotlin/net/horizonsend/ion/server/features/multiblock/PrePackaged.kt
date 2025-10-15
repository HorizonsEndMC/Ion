package net.horizonsend.ion.server.features.multiblock

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemContainerContents
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.custom.items.misc.MultiblockToken
import net.horizonsend.ion.server.features.custom.items.misc.PackagedMultiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities.loadFromData
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities.setMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.shape.BlockRequirement
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TurretMultiblock
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK_ENTITY_DATA
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component.text
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.WallSign
import org.bukkit.block.sign.Side
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.STRING
import org.bukkit.util.Vector

object PrePackaged : SLEventListener() {
	fun getOriginFromPlacement(clickedBlock: Block, direction: BlockFace, shape: MultiblockShape): Block {
		val requirements = shape.getRequirementMap(direction)
		// Place multiblocks that have blocks below the origin correctly
		val minY = requirements.entries.minOfOrNull { it.key.y } ?: throw NullPointerException("Multiblock has no shape!")

		return clickedBlock
			.getRelative(BlockFace.UP, 1) // Get the block on top of the clicked block
			// Min to cover turret condition, lowest block is significantly above sign
			.getRelative(BlockFace.DOWN, minOf(minY, 0)) // Go up (down negative blocks) until the origin is high enough to fit the blocks below it
	}

	/**
	 * @param allowCorrect Allows for correctly placed blocks that are already in the world to be ignored as obstructions
	 **/
	fun checkObstructions(origin: Block, direction: BlockFace, shape: MultiblockShape, allowCorrect: Boolean): List<Vec3i> {
		val requirements = shape.getRequirementMap(direction)
		val obstructed = mutableListOf<Vec3i>()

		for ((offset, requirement) in requirements) {
			val offsetBlock = origin.getRelativeIfLoaded(offset.x, offset.y, offset.z)
			val vec = Vec3i(origin.x, origin.y, origin.z).plus(offset)

			if (offsetBlock == null) {
				obstructed.add(vec)
				continue
			}

			if (allowCorrect) {
				if (!requirement.invoke(offsetBlock, direction, false) && !offsetBlock.type.isAir) obstructed.add(vec)
			} else if (!offsetBlock.type.isAir) {
				obstructed.add(vec)
			}
		}

		val signPosition = origin.getRelative(direction.oppositeFace, 1)
		if (!signPosition.type.isAir) {
			obstructed.add(Vec3i(signPosition.x, signPosition.y, signPosition.z))
		}

		return obstructed
	}

	val cooldown = PerPlayerCooldown(100L)

	fun place(player: Player, origin: Block, direction: BlockFace, multiblock: Multiblock, itemSource: List<ItemStack>?, entityData: PersistentMultiblockData?) {
		val requirements = multiblock.getExampleShape().getRequirementMap(direction)
		val placements = mutableMapOf<Block, BlockData>()

		for ((offset, requirement) in requirements) {
			val absolute = Vec3i(origin.x, origin.y, origin.z) + offset
			val (x, y, z) = absolute

			val existingBlock = origin.world.getBlockAt(x, y, z)

			// Already placed, assuming allow merges
			if (requirement.invoke(existingBlock, direction, false)) continue

			var usedItem: ItemStack? = null

			itemSource
				?.firstOrNull { requirement.itemRequirement.itemCheck(it) }
				?.let {
					usedItem = it.clone()
					requirement.itemRequirement.consume(it)
				}

			if (itemSource != null && usedItem == null) {
				player.userError("Packed multiblock was missing materials!")
				continue
			}

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

			val placement = if (usedItem == null) {
				requirement.example.clone()
			} else {
				requirement.itemRequirement.toBlock.invoke(usedItem!!)
			}

			requirement.executePlacementModifications(placement, direction)

			placements[existingBlock] = placement
		}

		for ((block, placement) in placements) {
			NewTransport.handleBlockEvent(block.world, block.x, block.y, block.z, block.blockData, placement, player.uniqueId)
			block.blockData = placement
			val soundGroup = placement.soundGroup
			origin.world.playSound(block.location, soundGroup.placeSound, soundGroup.volume, soundGroup.pitch)
		}

		if (multiblock is SignlessStarshipWeaponMultiblock<*>) return
		val signItem: ItemStack? = itemSource?.firstOrNull { it.type.isSign }

		// If there is an item source but no sign then there is not one available
		if (itemSource != null && signItem == null) return

		val signType = signItem?.type?.let { getWallSignType(it) } ?: Material.OAK_WALL_SIGN

		// Add sign
		val signPosition = origin.getRelative(direction.oppositeFace, if (multiblock.getExampleShape().signCentered) 0 else 1)
		val signData = signType.createBlockData { signData ->
			signData as Directional
			signData.facing = direction.oppositeFace
		}

		signPosition.blockData = signData

		val sign = signPosition.state as Sign

		if (entityData != null && multiblock is EntityMultiblock<*>) {
			entityData.x = origin.x
			entityData.y = origin.y
			entityData.z = origin.z
			entityData.signOffset = sign.getFacing().oppositeFace

			setMultiblockEntity(sign.world, origin.x, origin.y, origin.z) { manager ->
				val new = loadFromData(multiblock, manager, entityData)
				if (new is LegacyMultiblockEntity) new.loadFromSign(sign)

				new
			}
		}

		val signItemMeta = signItem?.itemMeta

		if (signItemMeta is BlockStateMeta && signItemMeta.hasBlockState()) {
			val accurateState = signItemMeta.blockState as Sign

			if (accurateState.persistentDataContainer.has(MULTIBLOCK)) {
				for ((line, component) in accurateState.front().lines().withIndex()) {
					sign.front().line(line, component)
				}

				accurateState.persistentDataContainer.get(MULTIBLOCK, STRING)?.let {
					sign.persistentDataContainer.set(MULTIBLOCK, STRING, it)
				}

				sign.update()

				signItem.amount--
				return
			}
		}

		signItem?.let { it.amount-- }

		// Set the detection name just in case the setup fails
		sign.getSide(Side.FRONT).line(0, text("[${multiblock.name}]"))
		sign.update()

		MultiblockAccess.tryDetectMultiblock(player, sign, false)
	}

	fun getTokenData(itemStack: ItemStack): Multiblock? {
		val data = itemStack.itemMeta.persistentDataContainer.get(MULTIBLOCK, PersistentDataType.TAG_CONTAINER) ?: return null
		val storageName = data.get(MULTIBLOCK, STRING)!!
		return MultiblockRegistration.getByStorageName(storageName)!!
	}

	fun setTokenData(multiblock: Multiblock, destination: PersistentDataContainer) {
		val pdc = destination.adapterContext.newPersistentDataContainer()
		pdc.set(MULTIBLOCK, STRING, multiblock.javaClass.simpleName)

		destination.set(MULTIBLOCK, PersistentDataType.TAG_CONTAINER, pdc)
	}

	/** Moves the needed materials for the provided multiblock from the list of items to the destination inventory */
	private fun packageFrom(items: List<ItemStack>, multiblock: Multiblock, destination: Inventory) {
		val itemRequirements = getRequirements(multiblock)
		val missing = mutableListOf<BlockRequirement>()

		for (blockRequirement in itemRequirements) {
			val requirement = blockRequirement.itemRequirement
			val success = items.firstOrNull { requirement.itemCheck(it) && requirement.consume(it.clone()) }

			if (success == null) {
				missing.add(blockRequirement)
				continue
			}

			val toAdd = success.clone()
			requirement.consume(success)

			val amount = requirement.amountConsumed(toAdd)
			LegacyItemUtils.addToInventory(destination, toAdd.asQuantity(amount))
		}
	}

	fun createPackagedItem(availableItems: List<ItemStack>, multiblock: Multiblock): ItemStack {
		val base = PackagedMultiblock.createFor(multiblock)

		@Suppress("UnstableApiUsage") val newState = Material.CHEST.createBlockData().createBlockState() as Chest
		packageFrom(availableItems, multiblock, newState.inventory)

		return base.updateData(DataComponentTypes.CONTAINER, ItemContainerContents.containerContents(newState.inventory.contents.filterNotNull()))
	}

	/**
	 * Returns a list of requirements not met by the existing items
	 **/
	fun checkRequirements(available: Iterable<ItemStack>, multiblock: Multiblock): List<BlockRequirement> {
		val items = available.mapTo(mutableListOf()) { it.clone() }

		val requirements = getRequirements(multiblock)
		val missing = mutableListOf<BlockRequirement>()

		for (blockRequirement in requirements) {
			val requirement = blockRequirement.itemRequirement
			if (items.any { requirement.itemCheck(it) && requirement.consume(it) }) continue
			missing.add(blockRequirement)
		}

		return missing
	}

	fun pickUpStructure(player: Player, sign: Sign) {
		val multiblockType = MultiblockAccess.getFast(sign)
		if (multiblockType == null) {
			player.userError("That sign isn't from a multiblock!")
			return
		}

		val entityData = (multiblockType as? EntityMultiblock<*>)?.getMultiblockEntity(sign)?.store()

		var structureDirection = sign.getFacing().oppositeFace
		val structureOrigin = multiblockType.getOriginBlock(sign)

		if (multiblockType is TurretMultiblock<*>) {
			val newFace = multiblockType.getFacingSafe(sign)
			if (newFace == null) {
				player.userError("Turret not intact!")
				return
			}

			structureDirection = newFace
		}

		// Structure already checked to get the face if turret
		if (multiblockType !is TurretMultiblock<*> && !multiblockType.getExampleShape().checkRequirements(structureOrigin, structureDirection, false)) {
			player.userError("Structure not intact!")
			return
		}

		val requirements = multiblockType.getExampleShape().getRequirementMap(structureDirection)

		val toBreak = mutableListOf<Block>()
		val items = mutableListOf<ItemStack>()

		val signItem = ItemStack(getNormalSignType(sign.type)).updateMeta { meta ->
			meta as BlockStateMeta
			meta.blockState = sign
		}

		for ((offset, requirement) in requirements) {
			val realBlock = structureOrigin.getRelativeIfLoaded(offset.x, offset.y, offset.z) ?: return
			if (!BlockBreakEvent(sign.block, player).callEvent()) return
			toBreak.add(realBlock)

			val item = requirement.itemRequirement.toItemStack(realBlock.blockData)
			items.add(item)
		}

		items.add(signItem)
		toBreak.add(sign.block)

		if (!BlockBreakEvent(sign.block, player).callEvent()) return

		toBreak.asReversed().forEach {
			val state = it.state
			if (state is InventoryHolder) {
				for (item in state.inventory.filterNotNull()) {
					state.world.dropItemNaturally(state.location.toCenterLocation(), item)
				}
			}

			it.setType(Material.AIR, false)
		}

		val item = createPackagedItem(items, multiblockType)

		if (entityData != null) {
			item.updatePersistentDataContainer() { set(MULTIBLOCK_ENTITY_DATA, PersistentMultiblockData, entityData) }
		}

		sign.world.dropItem(sign.location.toCenterLocation(), item)
	}

	private fun getRequirements(multiblock: Multiblock): List<BlockRequirement> = multiblock.getExampleShape().getRequirementMap(BlockFace.NORTH).values.plus(signRequirement)

	private val signRequirement = BlockRequirement(
		"any sign",
		Material.OAK_WALL_SIGN.createBlockData(),
		syncCheck@{ block, face, loadChunks ->
			val data = if (loadChunks) block.blockData else getBlockDataSafe(block.world, block.x, block.y, block.z) ?: return@syncCheck false
			if (data !is WallSign) return@syncCheck false
			if (data.facing != face.oppositeFace) return@syncCheck false
			true
		},
		BlockRequirement.ItemRequirement(
			itemCheck = { it.type.isSign },
			amountConsumed = { 1 },
			toBlock = { item ->
				val wallVariant = getWallSignType(item.type)
				wallVariant.createBlockData()
			},
			toItemStack = { ItemStack(getNormalSignType(it.material)) }
		)
	).addPlacementModification { blockFace, blockData ->
		blockData as WallSign
		blockData.facing = blockFace.oppositeFace
	}

	private fun getWallSignType(material: Material): Material {
		val variant = material.name.removeSuffix("_SIGN")
		return runCatching { Material.valueOf(variant + "_WALL_SIGN") }.getOrDefault(Material.OAK_WALL_SIGN)
	}

	private fun getNormalSignType(material: Material): Material {
		if (material.isSign && !material.isWallSign) return material
		val variant = material.name.removeSuffix("_WALL_SIGN")
		return runCatching { Material.valueOf(variant + "_SIGN") }.getOrDefault(Material.OAK_SIGN)
	}

	@EventHandler
	fun onCraftToken(event: PrepareItemCraftEvent) {
		val contents = event.inventory.contents.filterNot { it == null || it.isEmpty }
		if (contents.size != 1) return

		val item = contents.first() ?: return
		if (item.customItem != CustomItemKeys.PACKAGED_MULTIBLOCK) return

		val multiblock = getTokenData(item) ?: return
		event.inventory.result = MultiblockToken.constructFor(multiblock)
	}

	fun tryPreview(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (livingEntity !is Player) return

		val packagedData = PrePackaged.getTokenData(itemStack) ?: run {
			livingEntity.userError("The packaged multiblock has no data!")
			return
		}

		val face = livingEntity.facing

		val origin = getOriginFromPlacement(
			event.clickedBlock ?: return,
			face,
			packagedData.getExampleShape()
		)

		packagedData.getExampleShape().getRequirementMap(face).forEach { (coords, requirement) ->
			val requirementX = coords.x
			val requirementY = coords.y
			val requirementZ = coords.z

			val relative: Block = (if (!packagedData.getExampleShape().signCentered) origin else origin.getRelative(face.oppositeFace)).getRelative(requirementX, requirementY, requirementZ)

			val requirementMet = requirement(relative, face, false)

			if (!requirementMet) {
				val (xx, yy, zz) = Vec3i(relative.location)

				sendEntityPacket(livingEntity, displayBlock(livingEntity.world.minecraft, requirement.getExample(face), Vector(xx, yy, zz), 0.5f, Color.WHITE), 10 * 20L)
			}
		}
	}
}
